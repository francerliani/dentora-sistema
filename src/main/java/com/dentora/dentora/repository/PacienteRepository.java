package com.dentora.dentora.repository;

import com.dentora.dentora.model.ObraSocial;
import com.dentora.dentora.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Repositorio de pacientes.
// ¿Por qué NO hay un DireccionRepository? Porque en Paciente.java la relación con Direccion
// tiene cascade = CascadeType.ALL: al guardar un paciente, JPA guarda también su dirección.
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    // Pacientes activos (sin orden) — lo usa el reporte de pacientes por obra social
    List<Paciente> findByActivoTrue();

    // Pacientes activos ordenados por apellido y nombre (CU-02, paso 2)
    List<Paciente> findByActivoTrueOrderByApellidoAscNombreAsc();

    // Buscador: pacientes activos cuyo DNI o apellido contenga el texto (CU-02, 2a).
    // LOWER pasa todo a minúsculas, así "gonz" encuentra "González".
    // ORDER BY: el resultado sale ordenado por apellido y nombre
    @Query("SELECT p FROM Paciente p WHERE p.activo = true AND " +
           "(p.dni LIKE CONCAT('%', :texto, '%') OR " +
           "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY p.apellido, p.nombre")
    List<Paciente> buscar(@Param("texto") String texto);

    // ¿Existe algún paciente con este DNI? (alta)
    boolean existsByDni(String dni);

    // ¿Existe OTRO paciente con este DNI, sin contar a este mismo? (edición)
    boolean existsByDniAndIdPacienteNot(String dni, Integer idPaciente);

    // Buscar un paciente por DNI exacto (formulario de turnos y reactivación, 8b)
    Optional<Paciente> findByDni(String dni);

    // Cuántos pacientes tienen esa obra social (para no quitarla si está en uso, CU-04 C2a)
    long countByObraSocial(ObraSocial obraSocial);
}