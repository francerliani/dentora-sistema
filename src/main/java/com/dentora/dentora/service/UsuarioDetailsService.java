package com.dentora.dentora.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.UsuarioRepository;

@Service
// @Service: clase con lógica del sistema. Spring la crea sola y la usa en el login
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Spring llama a este método cuando alguien intenta iniciar sesión.
        // "username" es lo que se escribió en el campo Usuario del formulario

        // 1. Buscar el usuario en la base. Si no existe, se lanza un error
        //    y Spring muestra "usuario o contraseña incorrectos"
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Convertir NUESTRO Usuario al formato que entiende Spring Security (UserDetails)
        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getContrasena())      // la encriptada; Spring la compara con BCrypt
                .roles(usuario.getRol().getNombre())    // ej: "SECRETARIA" -> Spring la guarda como "ROLE_SECRETARIA"
                .disabled(!usuario.getActivo())         // si activo = false, la cuenta queda bloqueada
                .build();
    }
}
