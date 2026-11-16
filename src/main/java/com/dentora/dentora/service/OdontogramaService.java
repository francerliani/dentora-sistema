package com.dentora.dentora.service;

import com.dentora.dentora.model.Cara;
import com.dentora.dentora.model.DetalleTratamiento;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.repository.DetalleTratamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OdontogramaService {

    private final DetalleTratamientoRepository detalleRepository;

    public OdontogramaService(DetalleTratamientoRepository detalleRepository) {
        this.detalleRepository = detalleRepository;
    }

    // RN-14: el odontograma se reconstruye con el último tratamiento de cada cara.
    // CAMBIO: además del estado, manda los datos del tratamiento para el autocompletado
    @Transactional(readOnly = true)
    public List<Map<String, Object>> estadoActual(Paciente paciente) {

        Map<String, Map<String, Object>> porCara = new LinkedHashMap<>();

        for (DetalleTratamiento d : detalleRepository
                .findByConsulta_PacienteOrderByConsulta_FechaAscIdDetalleAsc(paciente)) {

            int numero = d.getDiente().getNumeroFDI();
            String estado = d.getEstado().getNombre().equals("Realizado") ? "realizado" : "a-realizar";

            for (Cara c : d.getCaras()) {
                String cara = c.getNombre().toLowerCase();

                // HashMap (y no Map.of) porque Map.of no acepta valores null
                Map<String, Object> info = new HashMap<>();
                info.put("diente", numero);
                info.put("cara", cara);
                info.put("estado", estado);
                // NUEVO: datos para el autocompletado
                info.put("idDetalle", d.getIdDetalle());   // para encontrar las otras caras del mismo tratamiento
                info.put("idTipo", d.getTipoTratamiento().getIdTipoTrat());
                info.put("tipoNombre", d.getTipoTratamiento().getNombre());
                info.put("diagnostico", d.getDiagnostico() != null ? d.getDiagnostico() : "");

                porCara.put(numero + "-" + cara, info);
            }
        }
        return new ArrayList<>(porCara.values());
    }
}