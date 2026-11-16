package com.dentora.dentora.controller;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.model.UsuarioForm;
import com.dentora.dentora.repository.*;
import com.dentora.dentora.service.UsuarioService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.dentora.dentora.service.DisponibilidadService;
import java.time.LocalTime;
import com.dentora.dentora.service.CatalogoService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class ConfiguracionController {

    // Orden de los días para mostrar las franjas (alfabéticamente "JUEVES" quedaría antes que "LUNES")
    private static final List<String> DIAS =
            List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO");

    // Cómo se muestra cada día en pantalla
    private static final Map<String, String> NOMBRES_DIAS = Map.of(
            "LUNES", "Lunes", "MARTES", "Martes", "MIERCOLES", "Miércoles",
            "JUEVES", "Jueves", "VIERNES", "Viernes", "SABADO", "Sábado", "DOMINGO", "Domingo");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final TipoTratamientoRepository tipoTratamientoRepository;
    private final UsuarioService usuarioService;
    private final DisponibilidadService disponibilidadService;
    private final CatalogoService catalogoService;  

        public ConfiguracionController(UsuarioRepository usuarioRepository,
                                   RolRepository rolRepository,
                                   DisponibilidadRepository disponibilidadRepository,
                                   ObraSocialRepository obraSocialRepository,
                                   TipoTratamientoRepository tipoTratamientoRepository,
                                   UsuarioService usuarioService,
                                   DisponibilidadService disponibilidadService,
                                   CatalogoService catalogoService) {
                                    
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.tipoTratamientoRepository = tipoTratamientoRepository;
        this.usuarioService = usuarioService;
        this.disponibilidadService = disponibilidadService;
        this.catalogoService = catalogoService;
    }

    // =========================================================
    // LA PÁGINA CON LAS 3 PESTAÑAS (paso 2)
    // /configuracion?tab=disponibilidad&idOdontologo=3 → abre esa pestaña y ese odontólogo
    // =========================================================
    @GetMapping("/configuracion")
    public String ver(@RequestParam(defaultValue = "usuarios") String tab,
                      @RequestParam(required = false) Integer idOdontologo,
                      Authentication auth, Model model) {

        // ----- Pestaña Usuarios -----
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByNombre();
        // Ids de los usuarios a los que NO se les muestra "Desactivar" (3d)
        List<Integer> protegidos = usuarios.stream()
                .filter(u -> usuarioService.esProtegido(u, auth.getName()))
                .map(Usuario::getIdUsuario)
                .toList();

        // ----- Pestaña Disponibilidad -----
        List<Usuario> odontologos = usuarioRepository.findByRol_NombreAndActivoTrue("ODONTOLOGO");
        Usuario odontologo = null;
        if (idOdontologo != null) {
            odontologo = usuarioRepository.findById(idOdontologo).orElse(null);
        } else if (!odontologos.isEmpty()) {
            odontologo = odontologos.get(0);
        }
        // Franjas ordenadas por día y hora de inicio (D2)
        List<Disponibilidad> franjas = (odontologo == null) ? List.of()
                : disponibilidadRepository.findByOdontologo(odontologo).stream()
                    .sorted(Comparator.comparing((Disponibilidad d) -> DIAS.indexOf(d.getDiaSemana()))
                                      .thenComparing(Disponibilidad::getHoraInicio))
                    .toList();

        model.addAttribute("tab", tab);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("protegidos", protegidos);
        model.addAttribute("odontologos", odontologos);
        model.addAttribute("odontologo", odontologo);
        model.addAttribute("franjas", franjas);
        model.addAttribute("dias", DIAS);
        model.addAttribute("nombresDias", NOMBRES_DIAS);
        // ----- Pestaña Catálogos (orden alfabético, C2) -----
        model.addAttribute("obrasSociales", obraSocialRepository.findAll(Sort.by("nombre")));
        model.addAttribute("tipos", tipoTratamientoRepository.findAll(Sort.by("nombre")));
        return "configuracion";
    }

    // =========================================================
    // USUARIOS
    // =========================================================

    // Paso 3-4: formulario vacío
    @GetMapping("/configuracion/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("form", new UsuarioForm());
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("rolBloqueado", false);
        return "usuario-form";
    }

    // 3a: formulario con los datos cargados (la contraseña va VACÍA)
    @GetMapping("/configuracion/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Integer id, Authentication auth, Model model) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) {
            return "redirect:/configuracion";
        }

        UsuarioForm form = new UsuarioForm();
        form.setIdUsuario(u.getIdUsuario());
        form.setNombre(u.getNombre());
        form.setUsuario(u.getUsuario());
        form.setIdRol(u.getRol().getIdRol());

        model.addAttribute("form", form);
        model.addAttribute("roles", rolRepository.findAll());
        // 3d: si es uno mismo o el único admin, el rol no se puede cambiar
        model.addAttribute("rolBloqueado", usuarioService.esProtegido(u, auth.getName()));
        return "usuario-form";
    }

    // Pasos 5 a 8: guardar (alta o edición)
    @PostMapping("/configuracion/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute("form") UsuarioForm form, Authentication auth,
                                 Model model, RedirectAttributes redirect) {
        try {
            usuarioService.guardar(form, auth.getName());
            redirect.addFlashAttribute("mensaje",
                    form.getIdUsuario() == null ? "Usuario creado correctamente." : "Usuario actualizado.");
            return "redirect:/configuracion?tab=usuarios";

        } catch (IllegalArgumentException e) {
            // 6a / 7a: volver al formulario con el error. Las contraseñas se vacían por seguridad
            form.setContrasena(null);
            form.setRepetirContrasena(null);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", rolRepository.findAll());
            Usuario existente = (form.getIdUsuario() == null) ? null
                    : usuarioRepository.findById(form.getIdUsuario()).orElse(null);
            model.addAttribute("rolBloqueado",
                    existente != null && usuarioService.esProtegido(existente, auth.getName()));
            return "usuario-form";
        }
    }

    // 3b: desactivar
    @PostMapping("/configuracion/usuarios/{id}/desactivar")
    public String desactivarUsuario(@PathVariable Integer id, Authentication auth, RedirectAttributes redirect) {
        try {
            usuarioService.desactivar(id, auth.getName());
            redirect.addFlashAttribute("mensaje", "Usuario desactivado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=usuarios";
    }

    // 3c: activar (y desbloquear)
    @PostMapping("/configuracion/usuarios/{id}/activar")
    public String activarUsuario(@PathVariable Integer id, RedirectAttributes redirect) {
        usuarioService.activar(id);
        redirect.addFlashAttribute("mensaje", "Usuario activado. Si estaba bloqueado, ya puede ingresar.");
        return "redirect:/configuracion?tab=usuarios";
    }

        // =========================================================
    // DISPONIBILIDAD
    // =========================================================

    // D3 a D6: agregar franja
    @PostMapping("/configuracion/disponibilidad/agregar")
    public String agregarFranja(@RequestParam Integer idOdontologo,
                                @RequestParam(required = false) String diaSemana,
                                @RequestParam(required = false) LocalTime horaInicio,
                                @RequestParam(required = false) LocalTime horaFin,
                                RedirectAttributes redirect) {
        try {
            disponibilidadService.agregar(idOdontologo, diaSemana, horaInicio, horaFin);
            redirect.addFlashAttribute("mensaje", "Horario agregado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // Volvemos a la pestaña Disponibilidad del mismo odontólogo
        return "redirect:/configuracion?tab=disponibilidad&idOdontologo=" + idOdontologo;
    }

    // D2a: quitar franja
    @PostMapping("/configuracion/disponibilidad/{id}/quitar")
    public String quitarFranja(@PathVariable Integer id, RedirectAttributes redirect) {
        // Antes de borrarla, guardamos de qué odontólogo era (para volver a su pestaña)
        Disponibilidad franja = disponibilidadRepository.findById(id).orElse(null);
        if (franja == null) {
            return "redirect:/configuracion?tab=disponibilidad";
        }
        Integer idOdontologo = franja.getOdontologo().getIdUsuario();

        long turnos = disponibilidadService.quitar(id);
        redirect.addFlashAttribute("mensaje", turnos > 0
                ? "Horario quitado. Hay " + turnos + " turnos asignados en esta franja; se conservan."
                : "Horario quitado.");

        return "redirect:/configuracion?tab=disponibilidad&idOdontologo=" + idOdontologo;
    }

        // =========================================================
    // CATÁLOGOS
    // =========================================================

    @PostMapping("/configuracion/obras-sociales/agregar")
    public String agregarObraSocial(@RequestParam String nombre, RedirectAttributes redirect) {
        try {
            catalogoService.agregarObraSocial(nombre);
            redirect.addFlashAttribute("mensaje", "Obra social agregada.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=catalogos";
    }

    @PostMapping("/configuracion/obras-sociales/{id}/quitar")
    public String quitarObraSocial(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            catalogoService.quitarObraSocial(id);
            redirect.addFlashAttribute("mensaje", "Obra social quitada.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=catalogos";
    }

       @PostMapping("/configuracion/tipos-tratamiento/agregar")
    public String agregarTipo(@RequestParam String nombre,
                              @RequestParam(required = false) BigDecimal precio,
                              RedirectAttributes redirect) {
        try {
            catalogoService.agregarTipo(nombre, precio);
            redirect.addFlashAttribute("mensaje", "Tratamiento agregado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=catalogos";
    }

    // NUEVO: actualizar el precio
    @PostMapping("/configuracion/tipos-tratamiento/{id}/precio")
    public String actualizarPrecio(@PathVariable Integer id,
                                   @RequestParam(required = false) BigDecimal precio,
                                   RedirectAttributes redirect) {
        try {
            catalogoService.actualizarPrecio(id, precio);
            redirect.addFlashAttribute("mensaje", "Precio actualizado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=catalogos";
    }

    @PostMapping("/configuracion/tipos-tratamiento/{id}/quitar")
    public String quitarTipo(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            catalogoService.quitarTipo(id);
            redirect.addFlashAttribute("mensaje", "Tratamiento quitado.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion?tab=catalogos";
    }
}