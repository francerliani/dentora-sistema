package com.dentora.dentora.config;

import com.dentora.dentora.repository.UsuarioRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

// Spring Security "avisa" (publica un evento) cada vez que alguien intenta loguearse.
// Esta clase escucha esos avisos y actualiza el contador en la base.
@Component
public class IntentosLoginListener {

    private final UsuarioRepository usuarioRepository;

    public IntentosLoginListener(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Se ejecuta cuando la contraseña es INCORRECTA
    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        String nombreUsuario = evento.getAuthentication().getName();

        // Si el usuario existe, le sumamos 1 al contador
        // (si escribieron un usuario que no existe, no hay nada que sumar)
        usuarioRepository.findByUsuario(nombreUsuario).ifPresent(u -> {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            usuarioRepository.save(u);
        });
    }

    // Se ejecuta cuando el login es CORRECTO
    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        String nombreUsuario = evento.getAuthentication().getName();

        // Entró bien: el contador vuelve a 0 (los fallos tienen que ser SEGUIDOS)
        usuarioRepository.findByUsuario(nombreUsuario).ifPresent(u -> {
            if (u.getIntentosFallidos() != 0) {
                u.setIntentosFallidos(0);
                usuarioRepository.save(u);
            }
        });
    }
}