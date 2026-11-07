package com.dentora.dentora.repository;

import com.dentora.dentora.model.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoTurnoRepository extends JpaRepository<EstadoTurno, Integer> {

    // Buscar un estado por su nombre. Ej: findByNombre("Pendiente")
    Optional<EstadoTurno> findByNombre(String nombre);
}