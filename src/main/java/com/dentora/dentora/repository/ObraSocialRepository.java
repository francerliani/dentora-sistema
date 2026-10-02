package com.dentora.dentora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentora.dentora.model.ObraSocial;

// Igual que SexoRepository, pero para el catálogo de obras sociales.
// findAll() va a llenar el desplegable "Obra social" del formulario
public interface ObraSocialRepository extends JpaRepository<ObraSocial, Integer> {
}