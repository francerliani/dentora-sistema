package com.dentora.dentora.config;

import com.dentora.dentora.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// @ControllerAdvice: lo que está acá se aplica a TODOS los controladores.
// Este método se ejecuta antes de cada página y agrega los datos del usuario logueado,
// así la barra y el menú los tienen siempre disponibles
@ControllerAdvice
public class DatosSesionAdvice {

    private final UsuarioRepository usuarioRepository;

    public DatosSesionAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute
    public void datosDeSesion(Authentication auth, Model model) {
        // En el login todavía no hay nadie logueado: no hace nada
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        usuarioRepository.findByUsuario(auth.getName()).ifPresent(u -> {
            String rol = u.getRol().getNombre();
            model.addAttribute("usuarioNombre", u.getNombre());   // ej: "Carlos Pérez"
            model.addAttribute("usuarioRol", rol);                // ej: "ADMINISTRADOR" (para el menú)
            model.addAttribute("usuarioRolTexto", switch (rol) {  // ej: "Administrador" (para mostrar)
                case "ODONTOLOGO" -> "Odontólogo";
                case "SECRETARIA" -> "Secretaria";
                default -> "Administrador";
            });
        });
    }
}