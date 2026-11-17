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

        // Todos los usuarios ordenados por nombre (tabla de la pestaña Usuarios)
    List<Usuario> findAllByOrderByNombre();

    // ¿Ya existe ese nombre de usuario? (alta, 7a)
    boolean existsByUsuario(String usuario);

    // ¿Lo tiene OTRO usuario? (edición, 7a)
    boolean existsByUsuarioAndIdUsuarioNot(String usuario, Integer idUsuario);

    // Cuántos usuarios activos hay con un rol (para saber si es el único Administrador, 3d)
    long countByRol_NombreAndActivoTrue(String nombreRol);

            // Usuarios activos de un rol, ordenados por nombre (CU-03, paso 2)
    List<Usuario> findByRol_NombreAndActivoTrueOrderByNombre(String nombreRol);
}