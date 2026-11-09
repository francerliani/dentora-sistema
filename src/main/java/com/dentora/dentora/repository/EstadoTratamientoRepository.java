package com.dentora.dentora.repository;

import com.dentora.dentora.model.EstadoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoTratamientoRepository extends JpaRepository<EstadoTratamiento, Integer> {
    Optional<EstadoTratamiento> findByNombre(String nombre);
}