package com.dentora.dentora.service;

import com.dentora.dentora.model.DetalleTratamiento;
import com.dentora.dentora.model.FilaReporte;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.repository.DetalleTratamientoRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Arma los reportes y estadísticas (devolución de los profesores)
@Service
public class ReporteService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final DetalleTratamientoRepository detalleRepository;

    public ReporteService(TurnoRepository turnoRepository, PacienteRepository pacienteRepository,
                          DetalleTratamientoRepository detalleRepository) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.detalleRepository = detalleRepository;
    }

    // 1) Cuántos turnos ATENDIDOS tuvo cada odontólogo en el período
    @Transactional(readOnly = true)
    public List<FilaReporte> turnosAtendidos(LocalDate desde, LocalDate hasta) {
        Map<String, FilaReporte> filas = new LinkedHashMap<>();
        for (Turno t : turnoRepository.findByEstado_NombreAndFechaBetween("Atendido", desde, hasta)) {
            // computeIfAbsent: si el odontólogo no tiene fila todavía, la crea
            filas.computeIfAbsent(t.getOdontologo().getNombre(), FilaReporte::new)
                 .sumar(BigDecimal.ZERO);
        }
        return ordenar(filas, false);
    }

    // 2) Cuántos pacientes ACTIVOS tiene cada obra social (los que no tienen = "Particular")
    @Transactional(readOnly = true)
    public List<FilaReporte> pacientesPorObraSocial() {
        Map<String, FilaReporte> filas = new LinkedHashMap<>();
        for (Paciente p : pacienteRepository.findByActivoTrue()) {
            String obra = (p.getObraSocial() != null) ? p.getObraSocial().getNombre() : "Particular";
            filas.computeIfAbsent(obra, FilaReporte::new).sumar(BigDecimal.ZERO);
        }
        return ordenar(filas, false);
    }

    // 3) Facturación: suma de los precios de los tratamientos REALIZADOS por cada odontólogo
    @Transactional(readOnly = true)
    public List<FilaReporte> facturacion(LocalDate desde, LocalDate hasta) {
        Map<String, FilaReporte> filas = new LinkedHashMap<>();
        for (DetalleTratamiento d : detalleRepository
                .findByEstado_NombreAndConsulta_FechaBetween("Realizado", desde, hasta)) {
            filas.computeIfAbsent(d.getConsulta().getOdontologo().getNombre(), FilaReporte::new)
                 .sumar(d.getTipoTratamiento().getPrecio());
        }
        return ordenar(filas, true);
    }

    // Ordena de mayor a menor y calcula el largo de cada barrita (el mayor = 100%)
    private List<FilaReporte> ordenar(Map<String, FilaReporte> filas, boolean porMonto) {
        List<FilaReporte> lista = new ArrayList<>(filas.values());

        if (porMonto) {
            lista.sort(Comparator.comparing(FilaReporte::getMonto).reversed());
        } else {
            lista.sort(Comparator.comparingLong(FilaReporte::getCantidad).reversed());
        }

        if (!lista.isEmpty()) {
            FilaReporte mayor = lista.get(0);
            for (FilaReporte f : lista) {
                int porcentaje = porMonto
                        ? (mayor.getMonto().signum() == 0 ? 0
                           : f.getMonto().multiply(BigDecimal.valueOf(100))
                                 .divide(mayor.getMonto(), 0, java.math.RoundingMode.HALF_UP).intValue())
                        : (int) (f.getCantidad() * 100 / mayor.getCantidad());
                f.setPorcentaje(porcentaje);
            }
        }
        return lista;
    }
}