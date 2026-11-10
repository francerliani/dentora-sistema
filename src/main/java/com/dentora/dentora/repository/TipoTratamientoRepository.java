package com.dentora.dentora.repository;

import com.dentora.dentora.model.TipoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoTratamientoRepository extends JpaRepository<TipoTratamiento, Integer> {

        boolean existsByNombreIgnoreCase(String nombre);
}