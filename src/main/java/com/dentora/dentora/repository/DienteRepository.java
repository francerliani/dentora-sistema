package com.dentora.dentora.repository;

import com.dentora.dentora.model.Diente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DienteRepository extends JpaRepository<Diente, Integer> {
    // Buscar una pieza por su número FDI. Ej: findByNumeroFDI(22)
    Optional<Diente> findByNumeroFDI(int numeroFDI);
}