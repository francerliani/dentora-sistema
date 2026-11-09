package com.dentora.dentora.repository;

import com.dentora.dentora.model.Consulta;
import com.dentora.dentora.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {
    // Historial clínico: consultas del paciente, de la más nueva a la más vieja
    List<Consulta> findByPacienteOrderByFechaDescIdConsultaDesc(Paciente paciente);
}