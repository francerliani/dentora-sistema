package com.dentora.dentora.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentora.dentora.model.Paciente;

// Repositorio principal del módulo.
// Por ahora alcanza con los métodos heredados (save, findAll, findById...).
// Más adelante le agregamos los métodos del buscador por DNI y apellido.
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
}

// ¿Por qué NO hay un DireccionRepository?
// Porque en Paciente.java la relación con Direccion tiene cascade = CascadeType.ALL:
// al guardar un paciente con pacienteRepository.save(...), JPA guarda su dirección
// automáticamente. La dirección nunca se maneja sola, siempre a través de su paciente.