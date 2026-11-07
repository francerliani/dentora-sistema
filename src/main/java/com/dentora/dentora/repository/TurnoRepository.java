package com.dentora.dentora.repository;

import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    // Los turnos de un odontólogo en una fecha, ordenados por hora (para armar la agenda)
    List<Turno> findByOdontologoAndFechaOrderByHora(Usuario odontologo, LocalDate fecha);
}