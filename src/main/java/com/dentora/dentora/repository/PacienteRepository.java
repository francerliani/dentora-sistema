package com.dentora.dentora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.dentora.dentora.model.Paciente;
import java.util.List;

// Repositorio principal del módulo.
// Por ahora alcanza con los métodos heredados (save, findAll, findById...).
// Más adelante le agregamos los métodos del buscador por DNI y apellido.
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {


// Trae solo los pacientes activos (los dados de baja no se muestran)
List<Paciente> findByActivoTrue();
// ¿Por qué NO hay un DireccionRepository?
// Porque en Paciente.java la relación con Direccion tiene cascade = CascadeType.ALL:
// al guardar un paciente con pacienteRepository.save(...), JPA guarda su dirección
// automáticamente. La dirección nunca se maneja sola, siempre a través de su paciente.

// Busca pacientes activos cuyo DNI o apellido contenga el texto escrito.
// LOWER pasa todo a minúsculas, así "gonz" encuentra "González".
@Query("SELECT p FROM Paciente p WHERE p.activo = true AND " +
       "(p.dni LIKE CONCAT('%', :texto, '%') OR " +
       "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :texto, '%')))")
List<Paciente> buscar(@Param("texto") String texto);

// ¿Existe algún paciente con este DNI? (para el alta)
boolean existsByDni(String dni);

// ¿Existe OTRO paciente con este DNI, sin contar a este mismo? (para editar, más adelante)
boolean existsByDniAndIdPacienteNot(String dni, Integer idPaciente);

// Buscar un paciente por DNI exacto (lo usa el formulario de turnos)
Optional<Paciente> findByDni(String dni);
}