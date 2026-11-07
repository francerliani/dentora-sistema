package com.dentora.dentora.controller;

import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.UsuarioRepository;
import com.dentora.dentora.service.AgendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TurnoController {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;   // NUEVO
    private final AgendaService agendaService;

    public TurnoController(UsuarioRepository usuarioRepository,
                           PacienteRepository pacienteRepository,
                           AgendaService agendaService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.agendaService = agendaService;
    }

    // ===== AGENDA (igual que antes) =====
    @GetMapping("/turnos")
    public String agenda(@RequestParam(required = false) Integer idOdontologo,
                         @RequestParam(required = false) LocalDate fecha,
                         Model model) {

        List<Usuario> odontologos = usuarioRepository.findByRol_NombreAndActivoTrue("ODONTOLOGO");

        if (fecha == null) {
            fecha = LocalDate.now();
        }

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
        return "turnos";
    }

    // ===== NUEVO: abrir el formulario =====
    // Desde "Asignar" llega precargado: /turnos/nuevo?idOdontologo=3&fecha=2026-11-09&hora=18:00
    // Desde "+ Nuevo turno" llega solo con odontólogo y fecha
    @GetMapping("/turnos/nuevo")
    public String nuevo(@RequestParam(required = false) Integer idOdontologo,
                        @RequestParam(required = false) LocalDate fecha,
                        @RequestParam(required = false) LocalTime hora,
                        Model model) {

        Turno turno = new Turno();   // duracionHoras ya arranca en 1
        if (idOdontologo != null) {
            turno.setOdontologo(usuarioRepository.findById(idOdontologo).orElse(null));
        }
        turno.setFecha(fecha != null ? fecha : LocalDate.now());
        turno.setHora(hora);

        model.addAttribute("turno", turno);
        model.addAttribute("textoPaciente", "");   // el buscador de paciente arranca vacío
        cargarListasFormulario(model);
        return "turno-form";
    }

    // ===== NUEVO: guardar =====
    @PostMapping("/turnos/guardar")
    public String guardar(@ModelAttribute Turno turno,
                          @RequestParam String textoPaciente,   // ej: "30456789 - González, María"
                          Model model, RedirectAttributes redirect) {

        // 1) Del texto del buscador nos quedamos con el DNI (lo que está antes del " - ")
        String dni = textoPaciente.split(" - ")[0].trim();
        Paciente paciente = pacienteRepository.findByDni(dni).orElse(null);
        turno.setPaciente(paciente);

        // 2) Validar todas las reglas
        String error = (paciente == null)
                ? "No se encontró el paciente. Elíjalo de la lista de sugerencias."
                : agendaService.validarTurno(turno);

        if (error != null) {
            // Volvemos al formulario con lo que había escrito y el mensaje
            model.addAttribute("error", error);
            model.addAttribute("textoPaciente", textoPaciente);
            cargarListasFormulario(model);
            return "turno-form";
        }

        // 3) Guardar (queda Pendiente) y volver a la agenda de ese día
        agendaService.guardarTurno(turno);
        redirect.addFlashAttribute("mensaje", "Turno guardado correctamente.");
        return "redirect:/turnos?idOdontologo=" + turno.getOdontologo().getIdUsuario()
                + "&fecha=" + turno.getFecha();
    }

    // Listas para los desplegables del formulario
    private void cargarListasFormulario(Model model) {
        model.addAttribute("pacientes", pacienteRepository.findByActivoTrue());   // solo activos (RN-05)
        model.addAttribute("odontologos", usuarioRepository.findByRol_NombreAndActivoTrue("ODONTOLOGO"));

        // Horas en punto de 08:00 a 20:00 (bloques de 1 hora)
        List<LocalTime> horas = new ArrayList<>();
        for (int h = 8; h <= 20; h++) {
            horas.add(LocalTime.of(h, 0));
        }
        model.addAttribute("horas", horas);
    }
}