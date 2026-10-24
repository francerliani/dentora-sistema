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

@Controller
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final SexoRepository sexoRepository;             // para el select de Sexo
    private final ObraSocialRepository obraSocialRepository; // para el select de Obra social

    public PacienteController(PacienteRepository pacienteRepository,
                              SexoRepository sexoRepository,
                              ObraSocialRepository obraSocialRepository) {
        this.pacienteRepository = pacienteRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
    }

    // ===== LISTADO + BUSCADOR =====
    @GetMapping("/pacientes")
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        if (buscar == null || buscar.isBlank()) {
            model.addAttribute("pacientes", pacienteRepository.findByActivoTrue());
        } else {
            model.addAttribute("pacientes", pacienteRepository.buscar(buscar.trim()));
        }
        model.addAttribute("buscar", buscar);
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
    @PostMapping("/pacientes/guardar")
    public String guardar(@ModelAttribute Paciente paciente, Model model,
                          RedirectAttributes redirect) {

        // 1) Regla de negocio: el DNI no se puede repetir
        boolean dniRepetido = (paciente.getIdPaciente() == null)
                ? pacienteRepository.existsByDni(paciente.getDni())
                : pacienteRepository.existsByDniAndIdPacienteNot(paciente.getDni(), paciente.getIdPaciente());

        if (dniRepetido) {
            // Volvemos al formulario con los datos cargados y un mensaje de error
            model.addAttribute("error", "Ya existe un paciente con el DNI " + paciente.getDni() + ".");
            cargarCatalogos(model);
            return "paciente-form";
        }

        // 2) Guardar. Por el cascade, también se guarda la dirección.
        pacienteRepository.save(paciente);

        // 3) Mensaje que se muestra una sola vez en el listado
        redirect.addFlashAttribute("mensaje", "Paciente guardado correctamente.");

        // 4) Redirigir al listado (así, si recargan, no se guarda dos veces)
        return "redirect:/pacientes";
    }

    // Manda a la página las listas para los dos <select>
    private void cargarCatalogos(Model model) {
        model.addAttribute("sexos", sexoRepository.findAll());
        model.addAttribute("obrasSociales", obraSocialRepository.findAll());
    }
}