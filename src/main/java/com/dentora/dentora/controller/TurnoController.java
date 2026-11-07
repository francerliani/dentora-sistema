package com.dentora.dentora.controller;

import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.UsuarioRepository;
import com.dentora.dentora.service.AgendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class TurnoController {

    private final UsuarioRepository usuarioRepository;
    private final AgendaService agendaService;

    public TurnoController(UsuarioRepository usuarioRepository, AgendaService agendaService) {
        this.usuarioRepository = usuarioRepository;
        this.agendaService = agendaService;
    }

    // /turnos → agenda de hoy del primer odontólogo
    // /turnos?idOdontologo=3&fecha=2026-11-09 → agenda elegida en los filtros
    @GetMapping("/turnos")
    public String agenda(@RequestParam(required = false) Integer idOdontologo,
                         @RequestParam(required = false) LocalDate fecha,
                         Model model) {

        List<Usuario> odontologos = usuarioRepository.findByRol_NombreAndActivoTrue("ODONTOLOGO");

        // Si no eligieron fecha, se usa hoy
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        // Si no eligieron odontólogo, se usa el primero de la lista
        Usuario odontologo = null;
        if (idOdontologo != null) {
            odontologo = usuarioRepository.findById(idOdontologo).orElse(null);
        } else if (!odontologos.isEmpty()) {
            odontologo = odontologos.get(0);
        }

        model.addAttribute("odontologos", odontologos);
        model.addAttribute("odontologo", odontologo);
        model.addAttribute("fecha", fecha);

        if (odontologo != null) {
            model.addAttribute("filas", agendaService.armarAgenda(odontologo, fecha));
        }

        return "turnos"; // templates/turnos.html
    }
}