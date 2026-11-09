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
}