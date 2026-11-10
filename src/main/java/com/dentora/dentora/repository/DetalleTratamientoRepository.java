package com.dentora.dentora.repository;

import com.dentora.dentora.model.DetalleTratamiento;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.TipoTratamiento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleTratamientoRepository extends JpaRepository<DetalleTratamiento, Integer> {
    // Todos los tratamientos del paciente, del más VIEJO al más NUEVO.
    // Para armar el odontograma los recorremos en ese orden y cada uno "pisa" al anterior,
    // así en cada cara queda el más reciente (RN-14)
    List<DetalleTratamiento> findByConsulta_PacienteOrderByConsulta_FechaAscIdDetalleAsc(Paciente paciente);

        // Cuántos tratamientos usan ese tipo (C2a)
    long countByTipoTratamiento(TipoTratamiento tipoTratamiento);
}