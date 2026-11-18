package com.dentora.dentora.controller;

import com.dentora.dentora.model.FilaReporte;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.UsuarioRepository;
import com.dentora.dentora.service.ReporteService;
import org.springframework.security.core.Authentication;
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
    private final UsuarioRepository usuarioRepository;   // NUEVO: para saber quién está logueado

    public ReporteController(ReporteService reporteService, UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.usuarioRepository = usuarioRepository;
    }

    // /reportes?anio=2026&mes=11 → noviembre 2026 · mes=0 → todo el año · sin datos → el mes actual
    @GetMapping("/reportes")
    public String ver(@RequestParam(required = false) Integer anio,
                      @RequestParam(required = false) Integer mes,
                      Authentication auth, Model model) {

        // NUEVO: el Odontólogo ve solo sus datos; el Administrador, los de todos
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElseThrow();
        boolean esOdontologo = usuario.getRol().getNombre().equals("ODONTOLOGO");
        Usuario soloDe = esOdontologo ? usuario : null;

        LocalDate hoy = LocalDate.now();
        if (anio == null) anio = hoy.getYear();
        if (mes == null) mes = hoy.getMonthValue();

        // Rango de fechas del período elegido (RN-21)
        LocalDate desde, hasta;
        if (mes == 0) {
            desde = LocalDate.of(anio, 1, 1);
            hasta = LocalDate.of(anio, 12, 31);
        } else {
            desde = LocalDate.of(anio, mes, 1);
            hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        }

        List<FilaReporte> turnos = reporteService.turnosAtendidos(desde, hasta, soloDe);
        List<FilaReporte> facturacion = reporteService.facturacion(desde, hasta, soloDe);

        model.addAttribute("esOdontologo", esOdontologo);
        model.addAttribute("anio", anio);
        model.addAttribute("mes", mes);
        model.addAttribute("anios", List.of(hoy.getYear() - 2, hoy.getYear() - 1, hoy.getYear()));
        model.addAttribute("turnos", turnos);
        model.addAttribute("facturacion", facturacion);
        model.addAttribute("totalTurnos", turnos.stream().mapToLong(FilaReporte::getCantidad).sum());
        model.addAttribute("totalFacturado", facturacion.stream()
                .map(FilaReporte::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));

        // Pacientes por obra social es un dato de TODO el centro: solo para el Administrador
        if (!esOdontologo) {
            List<FilaReporte> pacientes = reporteService.pacientesPorObraSocial();
            model.addAttribute("pacientes", pacientes);
            model.addAttribute("totalPacientes", pacientes.stream().mapToLong(FilaReporte::getCantidad).sum());
        }
        return "reportes";
    }
}