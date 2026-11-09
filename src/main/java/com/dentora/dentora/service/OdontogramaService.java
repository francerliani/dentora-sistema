package com.dentora.dentora.service;

import com.dentora.dentora.model.Cara;
import com.dentora.dentora.model.DetalleTratamiento;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.repository.DetalleTratamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OdontogramaService {

    private final DetalleTratamientoRepository detalleRepository;

    public OdontogramaService(DetalleTratamientoRepository detalleRepository) {
        this.detalleRepository = detalleRepository;
    }

    // RN-14: el odontograma NO se guarda; se reconstruye cada vez.
    // Devuelve una lista como la que usaba la maqueta:
    //   [ {diente: 16, cara: "oclusal", estado: "a-realizar"}, ... ]
    @Transactional(readOnly = true)
    public List<Map<String, Object>> estadoActual(Paciente paciente) {

        // Clave "diente-cara" (ej: "16-oclusal") → su último tratamiento.
        // Un Map no repite claves: si la cara aparece otra vez, el nuevo PISA al viejo.
        Map<String, Map<String, Object>> porCara = new LinkedHashMap<>();

        // Vienen ordenados del más VIEJO al más NUEVO → al final queda el más reciente
        for (DetalleTratamiento d : detalleRepository
                .findByConsulta_PacienteOrderByConsulta_FechaAscIdDetalleAsc(paciente)) {

            int numero = d.getDiente().getNumeroFDI();
            // El JavaScript usa "a-realizar" y "realizado" (son las clases CSS del color)
            String estado = d.getEstado().getNombre().equals("Realizado") ? "realizado" : "a-realizar";

            for (Cara c : d.getCaras()) {
                String cara = c.getNombre().toLowerCase();   // "Oclusal" → "oclusal"
                porCara.put(numero + "-" + cara,
                        Map.of("diente", numero, "cara", cara, "estado", estado));
            }
        }
        return new ArrayList<>(porCara.values());
    }
}