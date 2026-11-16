package com.dentora.dentora.repository;

import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    // Turnos de un odontólogo en una fecha (agenda)
    List<Turno> findByOdontologoAndFechaOrderByHora(Usuario odontologo, LocalDate fecha);

    // Turnos de un odontólogo en una fecha con un estado (ej: "Mis turnos de hoy")
    List<Turno> findByOdontologoAndFechaAndEstado_NombreOrderByHora(Usuario odontologo, LocalDate fecha, String estado);

    // NUEVO: turnos de UN paciente con un odontólogo, en una fecha y estado
    // (para el desplegable "Turno asociado" de la consulta, paso 7 del CU)
    List<Turno> findByPacienteAndOdontologoAndFechaAndEstado_NombreOrderByHora(
            Paciente paciente, Usuario odontologo, LocalDate fecha, String estado);

                // Cuántos turnos de un odontólogo tienen un estado desde una fecha en adelante
    // (ej: Pendientes desde hoy → no se lo puede desactivar, 3b)
    long countByOdontologoAndEstado_NombreAndFechaGreaterThanEqual(Usuario odontologo, String estado, LocalDate fecha);

            // Turnos de un odontólogo con un estado, desde una fecha en adelante
    // (para avisar cuántos turnos quedan en una franja que se quita, D2a)
    List<Turno> findByOdontologoAndEstado_NombreAndFechaGreaterThanEqual(Usuario odontologo, String estado, LocalDate fecha);

        // Turnos con un estado dentro de un rango de fechas (reporte de turnos atendidos)
    List<Turno> findByEstado_NombreAndFechaBetween(String estado, LocalDate desde, LocalDate hasta);
}