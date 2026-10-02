package com.dentora.dentora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentora.dentora.model.Sexo;

// JpaRepository<Sexo, Integer>:
//   Sexo    = la entidad que maneja este repositorio
//   Integer = el tipo de su clave primaria (idSexo)
// Hereda save(), findAll(), findById(), count(), deleteById()... sin escribir nada.
// findAll() lo vamos a usar para llenar el desplegable "Sexo" del formulario
public interface SexoRepository extends JpaRepository<Sexo, Integer> {
}