package com.dentora.dentora.repository;

import com.dentora.dentora.model.Cara;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CaraRepository extends JpaRepository<Cara, Integer> {
    Optional<Cara> findByNombre(String nombre);
}