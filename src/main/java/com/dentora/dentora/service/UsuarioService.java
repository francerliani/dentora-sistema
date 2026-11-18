package com.dentora.dentora.service;

import com.dentora.dentora.model.Rol;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.model.UsuarioForm;
import com.dentora.dentora.repository.RolRepository;
import com.dentora.dentora.repository.TurnoRepository;
import com.dentora.dentora.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// Reglas de negocio de la pestaña Usuarios (CU-04)
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;   // el mismo BCrypt del login

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          TurnoRepository turnoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.turnoRepository = turnoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // ALTA y EDICIÓN (pasos 5 a 8, alternativos 3a, 6a, 7a)
    // usuarioSesion = el nombre de usuario del Administrador logueado
    // =========================================================
    public void guardar(UsuarioForm f, String usuarioSesion) {
        boolean esNuevo = (f.getIdUsuario() == null);

        // Paso 6: campos completos y formato
        if (vacio(f.getNombre()) || vacio(f.getUsuario()) || f.getIdRol() == null) {
            throw new IllegalArgumentException("Complete todos los campos.");
        }
        if (f.getNombre().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre puede tener hasta 100 caracteres.");
        }
        // \\S{4,50} = entre 4 y 50 caracteres que NO sean espacios
        if (!f.getUsuario().trim().matches("\\S{4,50}")) {
            throw new IllegalArgumentException("El usuario debe tener entre 4 y 50 caracteres, sin espacios.");
        }

        // 3a: al editar, contraseña vacía = se conserva la actual
        boolean cambiaContrasena = esNuevo || !vacio(f.getContrasena());
        if (cambiaContrasena) {
            // (?=.*[A-Za-z]) = tiene al menos una letra; (?=.*\\d) = al menos un número; .{8,} = 8 o más
            if (f.getContrasena() == null || !f.getContrasena().matches("(?=.*[A-Za-z])(?=.*\\d).{8,}")) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, con letras y números.");
            }
            if (!f.getContrasena().equals(f.getRepetirContrasena())) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }
        }

        // Paso 7 / 7a: usuario único
        String nombreUsuario = f.getUsuario().trim();
        boolean repetido = esNuevo
                ? usuarioRepository.existsByUsuario(nombreUsuario)
                : usuarioRepository.existsByUsuarioAndIdUsuarioNot(nombreUsuario, f.getIdUsuario());
        if (repetido) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        Rol rol = rolRepository.findById(f.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol inexistente."));

        Usuario u;
        if (esNuevo) {
            // Paso 8: se guarda el HASH, nunca la contraseña tal cual (RN-01)
            u = new Usuario(f.getNombre().trim(), nombreUsuario, passwordEncoder.encode(f.getContrasena()), rol);
            u.setActivo(true);
            u.setIntentosFallidos(0);
        } else {
            u = usuarioRepository.findById(f.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

            // 3d: no se puede cambiar el rol de uno mismo ni del único Administrador activo
            boolean cambiaRol = !u.getRol().getNombre().equals(rol.getNombre());
            if (cambiaRol && esProtegido(u, usuarioSesion)) {
                throw new IllegalArgumentException(
                        "No se puede cambiar el rol del Administrador en sesión ni del único Administrador activo.");
            }

            u.setNombre(f.getNombre().trim());
            u.setUsuario(nombreUsuario);
            u.setRol(rol);
            if (cambiaContrasena) {
                u.setContrasena(passwordEncoder.encode(f.getContrasena()));
            }
        }
        usuarioRepository.save(u);
    }

    // =========================================================
    // DESACTIVAR (3b y 3d)
    // =========================================================
    public void desactivar(Integer idUsuario, String usuarioSesion) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        if (esProtegido(u, usuarioSesion)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar al Administrador en sesión ni al único Administrador activo.");
        }

        // 3b: un odontólogo con turnos pendientes futuros no se puede desactivar
        if (u.getRol().getNombre().equals("ODONTOLOGO")) {
            long pendientes = turnoRepository
                    .countByOdontologoAndEstado_NombreAndFechaGreaterThanEqual(u, "Pendiente", LocalDate.now());
            if (pendientes > 0) {
                throw new IllegalArgumentException("El odontólogo tiene " + pendientes
                        + " turnos pendientes. Cancélelos o reprográmelos antes.");
            }
        }

        u.setActivo(false);   // no se borra: sus turnos y consultas se conservan
        usuarioRepository.save(u);
    }

    // =========================================================
    // ACTIVAR (3c): también DESBLOQUEA la cuenta (intentos a 0)
    // =========================================================
    public void activar(Integer idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        u.setActivo(true);
        u.setIntentosFallidos(0);
        usuarioRepository.save(u);
    }

        // =========================================================
    // NUEVO: CAMBIAR MI CONTRASEÑA (cualquier usuario, la suya)
    // =========================================================
    public void cambiarContrasena(String usuarioSesion, String actual, String nueva, String repetir) {
        Usuario u = usuarioRepository.findByUsuario(usuarioSesion).orElseThrow();

        // 1) La actual tiene que ser correcta. matches() compara el texto con el hash BCrypt
        if (actual == null || !passwordEncoder.matches(actual, u.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        // 2) Mismo formato que en el alta de usuarios
        if (nueva == null || !nueva.matches("(?=.*[A-Za-z])(?=.*\\d).{8,}")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, con letras y números.");
        }
        if (!nueva.equals(repetir)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        if (passwordEncoder.matches(nueva, u.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña tiene que ser distinta de la actual.");
        }

        // 3) Se guarda el hash, nunca la contraseña tal cual (RN-01)
        u.setContrasena(passwordEncoder.encode(nueva));
        usuarioRepository.save(u);
    }

    // 3d: ¿es el Administrador en sesión, o el único Administrador activo?
    // (public porque la página también lo usa para ocultar el botón Desactivar)
    public boolean esProtegido(Usuario u, String usuarioSesion) {
        if (u.getUsuario().equals(usuarioSesion)) {
            return true;
        }
        return u.getRol().getNombre().equals("ADMINISTRADOR")
                && Boolean.TRUE.equals(u.getActivo())
                && usuarioRepository.countByRol_NombreAndActivoTrue("ADMINISTRADOR") <= 1;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}