package com.dentora.dentora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentora.dentora.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // findByUsuario -> SELECT * FROM usuario WHERE usuario = ?
    // Es el método que va a usar el login para buscar a quien intenta entrar
    Optional<Usuario> findByUsuario(String usuario);
}