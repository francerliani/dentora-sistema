package com.dentora.dentora.controller;

import com.dentora.dentora.model.ConsultaForm;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.ConsultaRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.TipoTratamientoRepository;
import com.dentora.dentora.repository.TurnoRepository;
import com.dentora.dentora.repository.UsuarioRepository;
import com.dentora.dentora.service.ConsultaService;
import com.dentora.dentora.service.OdontogramaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Period;

@Controller
public class OdontogramaController {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final ConsultaRepository consultaRepository;
    private final TipoTratamientoRepository tipoTratamientoRepository;   // NUEVO
    private final OdontogramaService odontogramaService;
    private final ConsultaService consultaService;                       // NUEVO

    public OdontogramaController(PacienteRepository pacienteRepository,
                                 UsuarioRepository usuarioRepository,
                                 TurnoRepository turnoRepository,
                                 ConsultaRepository consultaRepository,
                                 TipoTratamientoRepository tipoTratamientoRepository,
                                 OdontogramaService odontogramaService,
                                 ConsultaService consultaService) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.consultaRepository = consultaRepository;
        this.tipoTratamientoRepository = tipoTratamientoRepository;
        this.odontogramaService = odontogramaService;
        this.consultaService = consultaService;
    }

    @GetMapping("/odontograma")
    public String ver(@RequestParam(required = false) String buscar,
                      @RequestParam(required = false) Integer idPaciente,
                      Authentication auth, Model model) {

        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElseThrow();

        // ===== Sin paciente elegido: buscador =====
        if (idPaciente == null) {
            model.addAttribute("turnosHoy", turnoRepository
                    .findByOdontologoAndFechaAndEstado_NombreOrderByHora(usuario, LocalDate.now(), "Pendiente"));
            if (buscar != null && !buscar.isBlank()) {
                model.addAttribute("pacientes", pacienteRepository.buscar(buscar.trim()));
            }
            model.addAttribute("buscar", buscar);
            return "odontograma";
        }

        // ===== Paciente elegido =====
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        if (paciente == null) {
            return "redirect:/odontograma";
        }

        Integer edad = (paciente.getFechaNacimiento() != null)
                ? Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears()
                : null;

        model.addAttribute("paciente", paciente);
        model.addAttribute("edad", edad);
        model.addAttribute("tratamientos", odontogramaService.estadoActual(paciente));
        model.addAttribute("consultas", consultaRepository.findByPacienteOrderByFechaDescIdConsultaDesc(paciente));

        // NUEVO: datos para el formulario "Registrar consulta"
        model.addAttribute("tipos", tipoTratamientoRepository.findAll());
        model.addAttribute("turnosPaciente", turnoRepository
                .findByPacienteAndOdontologoAndFechaAndEstado_NombreOrderByHora(
                        paciente, usuario, LocalDate.now(), "Pendiente"));   // paso 7
        return "odontograma";
    }

    // ===== NUEVO: guardar la consulta =====
    // Está bajo /odontograma/... así ya queda protegida por la regla de SecurityConfig
    @PostMapping("/odontograma/consulta")
    public String registrarConsulta(@ModelAttribute ConsultaForm form,
                                    Authentication auth, RedirectAttributes redirect) {
        Usuario usuario = usuarioRepository.findByUsuario(auth.getName()).orElseThrow();

        try {
            consultaService.registrar(form, usuario);
            redirect.addFlashAttribute("mensaje", "Consulta registrada correctamente.");   // paso 18
        } catch (IllegalArgumentException e) {
            // Error de validación: mostramos el mensaje que armó el service
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Cualquier otro problema (ej: se cayó la base): alternativo 16a
            redirect.addFlashAttribute("error", "No se pudo registrar la consulta. Intente nuevamente.");
        }
        return "redirect:/odontograma?idPaciente=" + form.getIdPaciente();
    }
}