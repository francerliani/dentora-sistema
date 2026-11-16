package com.dentora.dentora.service;

import com.dentora.dentora.model.ObraSocial;
import com.dentora.dentora.model.TipoTratamiento;
import com.dentora.dentora.repository.DetalleTratamientoRepository;
import com.dentora.dentora.repository.ObraSocialRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.TipoTratamientoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

// Reglas de la pestaña Catálogos (CU-04, sub-flujo C)
@Service
public class CatalogoService {

    private final ObraSocialRepository obraSocialRepository;
    private final TipoTratamientoRepository tipoTratamientoRepository;
    private final PacienteRepository pacienteRepository;
    private final DetalleTratamientoRepository detalleRepository;

    public CatalogoService(ObraSocialRepository obraSocialRepository,
                           TipoTratamientoRepository tipoTratamientoRepository,
                           PacienteRepository pacienteRepository,
                           DetalleTratamientoRepository detalleRepository) {
        this.obraSocialRepository = obraSocialRepository;
        this.tipoTratamientoRepository = tipoTratamientoRepository;
        this.pacienteRepository = pacienteRepository;
        this.detalleRepository = detalleRepository;
    }

    // ===== OBRAS SOCIALES =====

    public void agregarObraSocial(String nombre) {
        String limpio = validarNombre(nombre);   // C4
        if (obraSocialRepository.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalArgumentException("Ya existe «" + limpio + "» en el catálogo.");
        }
        obraSocialRepository.save(new ObraSocial(limpio));   // C5
    }

    public void quitarObraSocial(Integer id) {
        ObraSocial os = obraSocialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La obra social no existe."));

        // C2a: si algún paciente la tiene, no se puede quitar
        long enUso = pacienteRepository.countByObraSocial(os);
        if (enUso > 0) {
            throw new IllegalArgumentException("No se puede quitar: está asignado a " + enUso + " registros.");
        }
        obraSocialRepository.delete(os);
    }

    // ===== TIPOS DE TRATAMIENTO =====

       public void agregarTipo(String nombre, BigDecimal precio) {
        String limpio = validarNombre(nombre);
        if (tipoTratamientoRepository.existsByNombreIgnoreCase(limpio)) {
            throw new IllegalArgumentException("Ya existe «" + limpio + "» en el catálogo.");
        }
        TipoTratamiento tipo = new TipoTratamiento(limpio);
        tipo.setPrecio(validarPrecio(precio));
        tipoTratamientoRepository.save(tipo);
    }

    // NUEVO: cambiar el precio de un tratamiento existente
    public void actualizarPrecio(Integer id, BigDecimal precio) {
        TipoTratamiento tipo = tipoTratamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El tratamiento no existe."));
        tipo.setPrecio(validarPrecio(precio));
        tipoTratamientoRepository.save(tipo);
    }

    // El precio es obligatorio y no puede ser negativo
    private BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Ingrese un precio válido (0 o mayor).");
        }
        return precio;
    }

    public void quitarTipo(Integer id) {
        TipoTratamiento tipo = tipoTratamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El tratamiento no existe."));

        // C2a: si algún DetalleTratamiento lo usa, no se puede quitar
        long enUso = detalleRepository.countByTipoTratamiento(tipo);
        if (enUso > 0) {
            throw new IllegalArgumentException("No se puede quitar: está asignado a " + enUso + " registros.");
        }
        tipoTratamientoRepository.delete(tipo);
    }

    // C4 / C4a: no vacío y hasta 100 caracteres. Devuelve el nombre sin espacios sobrantes
    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Ingrese un nombre.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 100) {
            throw new IllegalArgumentException("El nombre puede tener hasta 100 caracteres.");
        }
        return limpio;
    }
}