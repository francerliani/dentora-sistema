package com.dentora.dentora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentora.dentora.model.Rol;

// JpaRepository<Rol, Integer>:
//   Rol     = la entidad que maneja este repositorio
//   Integer = el tipo de su clave primaria (idRol)
// Solo con heredar de JpaRepository ya tenés: save(), findAll(), findById(), deleteById(), count()...
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Spring lee el NOMBRE del método y arma la consulta solo:
    // findByNombre -> SELECT * FROM rol WHERE nombre = ?
    // Optional = puede encontrar el rol o no (evita errores por null)
    Optional<Rol> findByNombre(String nombre);
}