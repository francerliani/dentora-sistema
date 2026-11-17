package com.dentora.dentora.controller;

import com.dentora.dentora.model.Direccion;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.repository.ObraSocialRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.SexoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;   // NUEVO: para leer el {id} de la dirección
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import com.dentora.dentora.repository.TurnoRepository;
import java.time.LocalDate;


@Controller
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final SexoRepository sexoRepository;             // para el select de Sexo
    private final ObraSocialRepository obraSocialRepository; // para el select de Obra social
    private final TurnoRepository turnoRepository;   // NUEVO: para controlar turnos pendientes

        public PacienteController(PacienteRepository pacienteRepository,
                              SexoRepository sexoRepository,
                              ObraSocialRepository obraSocialRepository,
                              TurnoRepository turnoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.turnoRepository = turnoRepository;
    }

        // ===== LISTADO + BUSCADOR + FILTRO POR OBRA SOCIAL =====
    // obra = "" (todas), "particular" (sin obra social) o el id de una obra social
    @GetMapping("/pacientes")
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(required = false) String obra,
                         Model model) {

        // 1) Primero el buscador (igual que antes)
        List<Paciente> pacientes = (buscar == null || buscar.isBlank())
                ? pacienteRepository.findByActivoTrue()
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
    // ===== ALTA: abrir el formulario vacío =====
    @GetMapping("/pacientes/nuevo")
    public String nuevo(Model model) {
        Paciente paciente = new Paciente();
        // Dirección vacía (con "Santa Fe" precargado) para que el formulario no falle
        paciente.setDireccion(new Direccion(null, null, "Santa Fe"));

        model.addAttribute("paciente", paciente);
        cargarCatalogos(model);
        return "paciente-form";
    }

    // ===== EDITAR: abrir el formulario con los datos del paciente =====
    // {id} es una parte variable de la dirección: /pacientes/editar/3 → id = 3
    @GetMapping("/pacientes/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {

        // Buscamos el paciente. Si no existe, volvemos al listado.
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
        return "paciente-form"; // el MISMO formulario del alta
    }

    // ===== GUARDAR: sirve para alta y para edición =====
    // Sin id → save() hace INSERT (nuevo). Con id → save() hace UPDATE (modifica).
       // ===== GUARDAR: alta y edición =====
    @PostMapping("/pacientes/guardar")
    public String guardar(@ModelAttribute Paciente paciente, Model model,
                          RedirectAttributes redirect) {

        // Limpieza del DNI: se sacan puntos y espacios ("35.000 205" → "35000205")
        String dni = (paciente.getDni() == null) ? "" : paciente.getDni().replaceAll("[.\\s]", "");
        paciente.setDni(dni);

        // Tiene que quedar con 7 u 8 números (7a)
        if (!dni.matches("\\d{7,8}")) {
            model.addAttribute("error", "El DNI debe tener 7 u 8 números, sin puntos ni espacios.");
            cargarCatalogos(model);
            return "paciente-form";
        }

        if (paciente.getIdPaciente() == null) {
            // ===== ALTA: ¿ya existe alguien con ese DNI? =====
            Paciente existente = pacienteRepository.findByDni(dni).orElse(null);
            if (existente != null) {
                if (Boolean.TRUE.equals(existente.getActivo())) {
                    // 8a: es un paciente activo → no se puede repetir
                    model.addAttribute("error", "Ya existe un paciente con ese DNI.");
                } else {
                    // 8b: es un paciente dado de baja → se ofrece reactivarlo
                    model.addAttribute("error", "El DNI corresponde a un paciente dado de baja. ¿Desea reactivarlo?");
                    model.addAttribute("reactivarId", existente.getIdPaciente());
                }
                cargarCatalogos(model);
                return "paciente-form";
            }
        } else {
            // ===== EDICIÓN: el DNI no puede ser el de OTRO paciente =====
            if (pacienteRepository.existsByDniAndIdPacienteNot(dni, paciente.getIdPaciente())) {
                model.addAttribute("error", "Ya existe un paciente con ese DNI.");
                cargarCatalogos(model);
                return "paciente-form";
            }
        }

        // Guardar. Por el cascade, también se guarda la dirección.
        pacienteRepository.save(paciente);
        redirect.addFlashAttribute("mensaje", "Paciente guardado correctamente.");
        return "redirect:/pacientes";
    }

    // ===== NUEVO: reactivar un paciente dado de baja (8b) =====
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

    // ===== DAR DE BAJA (baja lógica) =====
// Es POST y no GET porque MODIFICA datos: así nadie da de baja
// a un paciente solo por abrir un link
    // ===== DAR DE BAJA (baja lógica) =====
    @PostMapping("/pacientes/baja/{id}")
    public String darDeBaja(@PathVariable Integer id, RedirectAttributes redirect) {

        Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente == null) {
            return "redirect:/pacientes";
        }

        // NUEVO (3c): si tiene turnos Pendientes de hoy en adelante, no se puede dar de baja
        long pendientes = turnoRepository
                .countByPacienteAndEstado_NombreAndFechaGreaterThanEqual(paciente, "Pendiente", LocalDate.now());
        if (pendientes > 0) {
            redirect.addFlashAttribute("error", "El paciente tiene " + pendientes
                    + " turnos pendientes. Cancélelos antes de darlo de baja.");
            return "redirect:/pacientes";
        }

        paciente.setActivo(false);          // no se borra, se desactiva
        pacienteRepository.save(paciente);  // UPDATE en la base
        redirect.addFlashAttribute("mensaje",
                "Paciente " + paciente.getApellido() + ", " + paciente.getNombre() + " dado de baja.");
        return "redirect:/pacientes";
    }

    // Manda a la página las listas para los dos <select>
    private void cargarCatalogos(Model model) {
        model.addAttribute("sexos", sexoRepository.findAll());
        model.addAttribute("obrasSociales", obraSocialRepository.findAll());
    }
}