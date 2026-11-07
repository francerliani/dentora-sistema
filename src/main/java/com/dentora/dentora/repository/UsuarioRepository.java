package com.dentora.dentora.repository;

import com.dentora.dentora.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar un usuario por su nombre de usuario (lo usa el login) — ya lo tenías
    Optional<Usuario> findByUsuario(String usuario);

    // NUEVO: usuarios activos con un rol dado. Ej: findByRol_NombreAndActivoTrue("ODONTOLOGO")
    // Rol_Nombre = "el nombre del rol del usuario" (el _ entra a la relación)
    List<Usuario> findByRol_NombreAndActivoTrue(String nombreRol);
}