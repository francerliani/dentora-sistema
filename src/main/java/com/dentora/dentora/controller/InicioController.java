package com.dentora.dentora.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.TurnoRepository;
import com.dentora.dentora.repository.UsuarioRepository;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;   // NUEVO: para los turnos de hoy

    public InicioController(UsuarioRepository usuarioRepository, TurnoRepository turnoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/inicio"})
    public String inicio(Authentication authentication, Model model) {
        // Buscar en la base al usuario que inició sesión
        Usuario usuario = usuarioRepository.findByUsuario(authentication.getName())
                .orElseThrow();

        String rol = usuario.getRol().getNombre();   // "SECRETARIA"

        model.addAttribute("nombre", usuario.getNombre());                      // "Laura Gómez"
        model.addAttribute("primerNombre", usuario.getNombre().split(" ")[0]);  // "Laura"
        model.addAttribute("rol", rol);                                         // para el menú
        model.addAttribute("rolTexto", textoDelRol(rol));                       // "Secretaria"

        // NUEVO: turnos de hoy
        // - el odontólogo ve solo los suyos
        // - la secretaria y el administrador ven los de todos los odontólogos
        LocalDate hoy = LocalDate.now();
        List<Turno> turnosHoy = rol.equals("ODONTOLOGO")
                ? turnoRepository.findByOdontologoAndFechaOrderByHora(usuario, hoy)
                : turnoRepository.findByFechaOrderByHora(hoy);
        model.addAttribute("turnosHoy", turnosHoy);

        return "inicio";
    }   // ← acá TERMINA el método inicio

  
    // Método aparte, FUERA de inicio pero DENTRO de la clase
    private String textoDelRol(String rol) {
        return switch (rol) {
            case "ADMINISTRADOR" -> "Administrador";
            case "SECRETARIA"    -> "Secretaria";
            case "ODONTOLOGO"    -> "Odontólogo";
            default              -> rol;
        };
    }

}   // ← acá TERMINA la clase