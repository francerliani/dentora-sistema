package com.dentora.dentora.controller;

import com.dentora.dentora.model.Direccion;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.repository.ObraSocialRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.SexoRepository;
import com.dentora.dentora.repository.TurnoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

// Controlador del CU-02 Gestionar pacientes
@Controller
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final SexoRepository sexoRepository;             // para el select de Sexo
    private final ObraSocialRepository obraSocialRepository; // para el select de Obra social
    private final TurnoRepository turnoRepository;           // para controlar turnos pendientes (3c)

    public PacienteController(PacienteRepository pacienteRepository,
                              SexoRepository sexoRepository,
                              ObraSocialRepository obraSocialRepository,
                              TurnoRepository turnoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.turnoRepository = turnoRepository;
    }

    // ===== LISTADO + BUSCADOR + FILTRO POR OBRA SOCIAL (paso 2, 2a, 2b) =====
    // obra = "" (todas), "particular" (sin obra social) o el id de una obra social
    @GetMapping("/pacientes")
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(required = false) String obra,
                         Model model) {

        // 1) Primero el buscador. Los dos caminos devuelven la lista ordenada por apellido y nombre
        List<Paciente> pacientes = (buscar == null || buscar.isBlank())
                ? pacienteRepository.findByActivoTrueOrderByApellidoAscNombreAsc()
                : pacienteRepository.buscar(buscar.trim());

        // 2) Después el filtro de obra social, sobre ese resultado
        if ("particular".equals(obra)) {
            pacientes = pacientes.stream()
                    .filter(p -> p.getObraSocial() == null)
                    .toList();
        } else if (obra != null && !obra.isBlank()) {
            Integer idObra = Integer.valueOf(obra);
            pacientes = pacientes.stream()
                    .filter(p -> p.getObraSocial() != null
                              && idObra.equals(p.getObraSocial().getIdObraSocial()))
                    .toList();
        }

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("buscar", buscar);
        model.addAttribute("obra", obra);                                   // para que quede elegida
        model.addAttribute("obrasSociales", obraSocialRepository.findAll()); // opciones del filtro
        return "pacientes";
    }

    // ===== ALTA: abrir el formulario vacío (paso 4) =====
    @GetMapping("/pacientes/nuevo")
    public String nuevo(Model model) {
        Paciente paciente = new Paciente();
        // Dirección vacía (con "Santa Fe" precargado) para que el formulario no falle
        paciente.setDireccion(new Direccion(null, null, "Santa Fe"));

        model.addAttribute("paciente", paciente);
        cargarCatalogos(model);
        return "paciente-form";
    }

    // ===== EDITAR: abrir el formulario con los datos del paciente (3a) =====
    // {id} es una parte variable de la dirección: /pacientes/editar/3 → id = 3
    @GetMapping("/pacientes/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        // Si no tenía dirección cargada, le damos una vacía
        if (paciente.getDireccion() == null) {
            paciente.setDireccion(new Direccion(null, null, "Santa Fe"));
        }

        model.addAttribute("paciente", paciente);
        cargarCatalogos(model);
        return "paciente-form";   // el MISMO formulario del alta
    }
    // ===== GUARDAR: alta y edición (pasos 6 a 10) =====
    // Sin id → save() hace INSERT (nuevo). Con id → save() hace UPDATE (modifica).
    @PostMapping("/pacientes/guardar")
    public String guardar(@ModelAttribute Paciente paciente, Model model,
                          RedirectAttributes redirect) {

        // Paso 7: limpieza del DNI, se sacan puntos y espacios ("35.000 205" → "35000205")
        String dni = (paciente.getDni() == null) ? "" : paciente.getDni().replaceAll("[.\\s]", "");
        paciente.setDni(dni);

        // 7a: tiene que quedar con 7 u 8 números
        if (!dni.matches("\\d{7,8}")) {
            return volverAlFormulario(model, "El DNI debe tener 7 u 8 números, sin puntos ni espacios.");
        }

        // 7a: la fecha de nacimiento no puede ser futura
        if (paciente.getFechaNacimiento() != null && paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            return volverAlFormulario(model, "La fecha de nacimiento no puede ser futura.");
        }

        // Paso 8: ¿hay otro paciente con ese DNI?
        if (paciente.getIdPaciente() == null) {
            // ALTA
            Paciente existente = pacienteRepository.findByDni(dni).orElse(null);
            if (existente != null) {
                if (Boolean.TRUE.equals(existente.getActivo())) {
                    // 8a: es un paciente activo → no se puede repetir
                    return volverAlFormulario(model, "Ya existe un paciente con ese DNI.");
                }
                // 8b: es un paciente dado de baja → se ofrece reactivarlo
                model.addAttribute("reactivarId", existente.getIdPaciente());
                return volverAlFormulario(model, "El DNI corresponde a un paciente dado de baja. ¿Desea reactivarlo?");
            }
        } else if (pacienteRepository.existsByDniAndIdPacienteNot(dni, paciente.getIdPaciente())) {
            // EDICIÓN: el DNI no puede ser el de OTRO paciente
            return volverAlFormulario(model, "Ya existe un paciente con ese DNI.");
        }

        // Paso 9: guardar. Por el cascade, también se guarda la dirección.
        boolean esNuevo = (paciente.getIdPaciente() == null);   // se mira ANTES de guardar
        pacienteRepository.save(paciente);

        // Paso 10 / 3a
        redirect.addFlashAttribute("mensaje",
                esNuevo ? "Paciente registrado correctamente." : "Paciente modificado correctamente.");
        return "redirect:/pacientes";
    }

    // ===== REACTIVAR un paciente dado de baja (8b) =====
    @PostMapping("/pacientes/reactivar/{id}")
    public String reactivar(@PathVariable Integer id, RedirectAttributes redirect) {
        Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente == null) {
            return "redirect:/pacientes";
        }
        paciente.setActivo(true);
        pacienteRepository.save(paciente);

        // Abre su formulario de edición (3a) para que actualicen los datos
        redirect.addFlashAttribute("mensaje", "Paciente reactivado. Revisá y actualizá sus datos.");
        return "redirect:/pacientes/editar/" + id;
    }

    // ===== DAR DE BAJA: baja lógica (3b, 3c) =====
    // Es POST y no GET porque MODIFICA datos: así nadie da de baja a un paciente solo por abrir un link
    @PostMapping("/pacientes/baja/{id}")
    public String darDeBaja(@PathVariable Integer id, RedirectAttributes redirect) {
        Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente == null) {
            return "redirect:/pacientes";
        }

        // 3c: si tiene turnos Pendientes de hoy en adelante, no se puede dar de baja
        long pendientes = turnoRepository
                .countByPacienteAndEstado_NombreAndFechaGreaterThanEqual(paciente, "Pendiente", LocalDate.now());
        if (pendientes > 0) {
            redirect.addFlashAttribute("error", "El paciente tiene " + pendientes
                    + " turnos pendientes. Cancélelos antes de darlo de baja.");
            return "redirect:/pacientes";
        }

        paciente.setActivo(false);          // no se borra, se desactiva (RN-05)
        pacienteRepository.save(paciente);  // UPDATE en la base
        redirect.addFlashAttribute("mensaje",
                "Paciente " + paciente.getApellido() + ", " + paciente.getNombre() + " dado de baja.");
        return "redirect:/pacientes";
    }

    // ===== Métodos auxiliares =====

    // Vuelve al formulario con un mensaje de error, conservando lo que se cargó
    private String volverAlFormulario(Model model, String error) {
        model.addAttribute("error", error);
        cargarCatalogos(model);
        return "paciente-form";
    }

    // Manda a la página las listas para los dos <select>
    private void cargarCatalogos(Model model) {
        model.addAttribute("sexos", sexoRepository.findAll());
        model.addAttribute("obrasSociales", obraSocialRepository.findAll());
    }
}
