package com.dentora.dentora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.UsuarioRepository;

@Controller
public class InicioController {

    private final UsuarioRepository usuarioRepository;

    public InicioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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