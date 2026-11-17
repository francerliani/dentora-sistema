package com.dentora.dentora.controller;

import com.dentora.dentora.model.FilaAgenda;
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
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PacienteRepository pacienteRepository;
    private final AgendaService agendaService;

    public TurnoController(UsuarioRepository usuarioRepository,
                           PacienteRepository pacienteRepository,
                           AgendaService agendaService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.agendaService = agendaService;
    }

    // ===== AGENDA =====
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
        model.addAttribute("diaTexto", agendaService.nombreDia(fecha));   // NUEVO: para el mensaje 4a

        if (odontologo != null) {
            model.addAttribute("filas", agendaService.armarAgenda(odontologo, fecha));
        }
        return "turnos";
    }

    // ===== NUEVO TURNO: abrir el formulario =====
    // Se vuelve a llamar cada vez que cambian odontólogo o fecha (para recargar las horas libres).
    // textoPaciente y duracion viajan para no perder lo que ya se había completado.
    @GetMapping("/turnos/nuevo")
    public String nuevo(@RequestParam(required = false) Integer idOdontologo,
                        @RequestParam(required = false) LocalDate fecha,
                        @RequestParam(required = false) LocalTime hora,
                        @RequestParam(required = false) String textoPaciente,
                        @RequestParam(required = false) Integer duracion,
                        Model model) {

        Turno turno = new Turno();
        if (idOdontologo != null) {
            turno.setOdontologo(usuarioRepository.findById(idOdontologo).orElse(null));
        }
        turno.setFecha(fecha != null ? fecha : LocalDate.now());
        turno.setHora(hora);
        if (duracion != null) {
            turno.setDuracionHoras(duracion);
        }

        model.addAttribute("turno", turno);
        model.addAttribute("textoPaciente", textoPaciente != null ? textoPaciente : "");
        cargarListasFormulario(model, turno);
        return "turno-form";
    }

    // ===== GUARDAR =====
    @PostMapping("/turnos/guardar")
    public String guardar(@ModelAttribute Turno turno,
                          @RequestParam String textoPaciente,
                          Model model, RedirectAttributes redirect) {

        // Del texto del buscador nos quedamos con el DNI (lo que está antes del " - ")
        String dni = textoPaciente.split(" - ")[0].trim();
        Paciente paciente = pacienteRepository.findByDni(dni).orElse(null);
        turno.setPaciente(paciente);

        String error = (paciente == null)
                ? "No se encontró el paciente. Elíjalo de la lista de sugerencias."
                : agendaService.validarTurno(turno);

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("textoPaciente", textoPaciente);
            cargarListasFormulario(model, turno);
            return "turno-form";
        }

        boolean esNuevo = (turno.getIdTurno() == null);
        agendaService.guardarTurno(turno);
        redirect.addFlashAttribute("mensaje",
                esNuevo ? "Turno asignado correctamente." : "Turno reprogramado correctamente.");
        return "redirect:/turnos?idOdontologo=" + turno.getOdontologo().getIdUsuario()
                + "&fecha=" + turno.getFecha();
    }

        // ===== REPROGRAMAR: abrir el formulario con los datos del turno =====
    // También recibe odontólogo/fecha nuevos cuando se cambian en el formulario
    @GetMapping("/turnos/reprogramar/{id}")
    public String reprogramar(@PathVariable Integer id,
                              @RequestParam(required = false) Integer idOdontologo,
                              @RequestParam(required = false) LocalDate fecha,
                              @RequestParam(required = false) Integer duracion,
                              Model model) {
        Turno original = agendaService.buscarTurno(id);
        if (original == null) {
            return "redirect:/turnos";
        }

        // Trabajamos sobre una COPIA, así no se modifica el turno guardado hasta tocar "Guardar"
        Turno turno = new Turno();
        turno.setIdTurno(original.getIdTurno());
        turno.setPaciente(original.getPaciente());
        turno.setOdontologo(idOdontologo != null
                ? usuarioRepository.findById(idOdontologo).orElse(null) : original.getOdontologo());
        turno.setFecha(fecha != null ? fecha : original.getFecha());
        turno.setHora(fecha == null && idOdontologo == null ? original.getHora() : null);
        turno.setDuracionHoras(duracion != null ? duracion : original.getDuracionHoras());

        Paciente p = original.getPaciente();
        model.addAttribute("textoPaciente", p.getDni() + " - " + p.getApellido() + ", " + p.getNombre());
        model.addAttribute("turno", turno);
        cargarListasFormulario(model, turno);
        return "turno-form";
    }

    // ===== CANCELAR =====
    @PostMapping("/turnos/cancelar/{id}")
    public String cancelar(@PathVariable Integer id, RedirectAttributes redirect) {
        Turno turno = agendaService.cancelarTurno(id);
        if (turno == null) {
            return "redirect:/turnos";
        }
        redirect.addFlashAttribute("mensaje", "Turno de " + turno.getPaciente().getApellido()
                + " cancelado. El horario quedó libre.");
        return "redirect:/turnos?idOdontologo=" + turno.getOdontologo().getIdUsuario()
                + "&fecha=" + turno.getFecha();
    }

    // ===== Listas para los desplegables del formulario =====
    private void cargarListasFormulario(Model model, Turno turno) {
        model.addAttribute("pacientes", pacienteRepository.findByActivoTrue());   // solo activos (RN-05)
        model.addAttribute("odontologos", usuarioRepository.findByRol_NombreAndActivoTrue("ODONTOLOGO"));

        // NUEVO (6a): solo las horas LIBRES de ese odontólogo en esa fecha
        List<LocalTime> horas = new ArrayList<>();
        boolean hayDatos = turno.getOdontologo() != null && turno.getFecha() != null;

        if (hayDatos) {
            for (FilaAgenda fila : agendaService.armarAgenda(turno.getOdontologo(), turno.getFecha())) {
                // Al reprogramar, el horario del propio turno también se ofrece
                boolean esElPropio = !fila.isLibre() && turno.getIdTurno() != null
                        && turno.getIdTurno().equals(fila.getTurno().getIdTurno());
                if (fila.isLibre() || esElPropio) {
                    horas.add(fila.getHora());
                }
            }
            // Si es hoy, se sacan las horas que ya pasaron
            if (turno.getFecha().equals(LocalDate.now())) {
                horas.removeIf(h -> !h.isAfter(LocalTime.now()));
            }
        }

        model.addAttribute("horas", horas);
        model.addAttribute("hayDatos", hayDatos);
        model.addAttribute("sinHorarios", hayDatos && horas.isEmpty());   // 5a
    }
}