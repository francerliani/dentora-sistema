package com.dentora.dentora.controller;

import com.dentora.dentora.model.FilaReporte;
import com.dentora.dentora.service.ReporteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // /reportes?anio=2026&mes=11 → noviembre 2026
    // /reportes?anio=2026&mes=0  → todo el año 2026
    // /reportes                  → el mes actual
    @GetMapping("/reportes")
    public String ver(@RequestParam(required = false) Integer anio,
                      @RequestParam(required = false) Integer mes,
                      Model model) {

        LocalDate hoy = LocalDate.now();
        if (anio == null) anio = hoy.getYear();
        if (mes == null) mes = hoy.getMonthValue();

        // Rango de fechas del período elegido
        LocalDate desde, hasta;
        if (mes == 0) {
            desde = LocalDate.of(anio, 1, 1);
            hasta = LocalDate.of(anio, 12, 31);
        } else {
            desde = LocalDate.of(anio, mes, 1);
            hasta = desde.withDayOfMonth(desde.lengthOfMonth());   // último día de ese mes
        }

        List<FilaReporte> turnos = reporteService.turnosAtendidos(desde, hasta);
        List<FilaReporte> pacientes = reporteService.pacientesPorObraSocial();
        List<FilaReporte> facturacion = reporteService.facturacion(desde, hasta);

        model.addAttribute("anio", anio);
        model.addAttribute("mes", mes);
        model.addAttribute("anios", List.of(hoy.getYear() - 2, hoy.getYear() - 1, hoy.getYear()));
        model.addAttribute("turnos", turnos);
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("facturacion", facturacion);

        // Totales de cada reporte
        model.addAttribute("totalTurnos", turnos.stream().mapToLong(FilaReporte::getCantidad).sum());
        model.addAttribute("totalPacientes", pacientes.stream().mapToLong(FilaReporte::getCantidad).sum());
        model.addAttribute("totalFacturado", facturacion.stream()
                .map(FilaReporte::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
        return "reportes";
    }
}