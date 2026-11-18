package com.dentora.dentora.controller;

import com.dentora.dentora.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// "Mi cuenta": lo que cada usuario puede hacer con su propia cuenta
@Controller
public class CuentaController {

    private final UsuarioService usuarioService;

    public CuentaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Abrir el formulario
    @GetMapping("/mi-cuenta/contrasena")
    public String formulario() {
        return "cambiar-contrasena";
    }

    // Guardar la nueva contraseña
    @PostMapping("/mi-cuenta/contrasena")
    public String cambiar(@RequestParam String actual,
                          @RequestParam String nueva,
                          @RequestParam String repetir,
                          Authentication auth, RedirectAttributes redirect) {
        try {
            usuarioService.cambiarContrasena(auth.getName(), actual, nueva, repetir);
            redirect.addFlashAttribute("mensaje", "Contraseña actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mi-cuenta/contrasena";
    }
}