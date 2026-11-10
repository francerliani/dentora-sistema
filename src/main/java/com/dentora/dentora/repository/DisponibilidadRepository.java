package com.dentora.dentora.repository;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    // Las franjas de un odontólogo en un día. Ej: las de jgaite los LUNES
    List<Disponibilidad> findByOdontologoAndDiaSemanaOrderByHoraInicio(Usuario odontologo, String diaSemana);

        // Todas las franjas de un odontólogo (pestaña Disponibilidad)
    List<Disponibilidad> findByOdontologo(Usuario odontologo);
}