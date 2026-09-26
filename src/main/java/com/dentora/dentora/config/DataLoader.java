package com.dentora.dentora.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dentora.dentora.model.Rol;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.RolRepository;
import com.dentora.dentora.repository.UsuarioRepository;

@Component
// @Component: Spring crea esta clase automáticamente al arrancar
public class DataLoader implements CommandLineRunner {
    // CommandLineRunner: el método run() se ejecuta UNA vez, apenas termina de arrancar el sistema

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección por constructor: Spring le pasa solo los repositorios y el encriptador.
    // No hace falta hacer "new" de nada
    public DataLoader(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Si ya hay roles cargados, no hace nada.
        // Sin esto, se intentaría cargar todo de nuevo en cada reinicio
        if (rolRepository.count() > 0) {
            return;
        }

        // 1. Los tres roles del diagrama de casos de uso.
        //    save() devuelve el rol ya guardado, con su id asignado por la base
        Rol administrador = rolRepository.save(new Rol("ADMINISTRADOR"));
        Rol secretaria    = rolRepository.save(new Rol("SECRETARIA"));
        Rol odontologo    = rolRepository.save(new Rol("ODONTOLOGO"));

        // 2. Un usuario de prueba por rol, con la contraseña ENCRIPTADA
        usuarioRepository.save(new Usuario("Carlos Pérez", "cperez",
                passwordEncoder.encode("admin123"), administrador));

        usuarioRepository.save(new Usuario("Laura Gómez", "lgomez",
                passwordEncoder.encode("secre123"), secretaria));

        usuarioRepository.save(new Usuario("Juan Gaite", "jgaite",
                passwordEncoder.encode("odonto123"), odontologo));

        System.out.println(">>> Datos iniciales cargados: 3 roles y 3 usuarios");
    }
}