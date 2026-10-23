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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PacienteController {

    private final PacienteRepository pacienteRepository;
    private final SexoRepository sexoRepository;           // NUEVO: para el select de Sexo
    private final ObraSocialRepository obraSocialRepository; // NUEVO: para el select de Obra social

    public PacienteController(PacienteRepository pacienteRepository,
                              SexoRepository sexoRepository,
                              ObraSocialRepository obraSocialRepository) {
        this.pacienteRepository = pacienteRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
    }

    // ===== LISTADO + BUSCADOR (lo que ya tenías) =====
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

    // ===== NUEVO: abrir el formulario vacío =====
    @GetMapping("/pacientes/nuevo")
    public String nuevo(Model model) {
        Paciente paciente = new Paciente();
        // Le damos una dirección vacía (con "Santa Fe" precargado),
        // porque el formulario tiene campos de dirección y si fuera null daría error
        paciente.setDireccion(new Direccion(null, null, "Santa Fe"));

        model.addAttribute("paciente", paciente);
        cargarCatalogos(model);
        return "paciente-form"; // abre templates/paciente-form.html
    }

    // ===== NUEVO: recibe el formulario y guarda =====
        @PostMapping("/pacientes/guardar")
        public String guardar(@ModelAttribute Paciente paciente, Model model,
                      RedirectAttributes redirect) {
    // Spring ya armó el objeto "paciente" con lo que se escribió en el formulario.
    // Sexo y Obra social llegan como id y Spring busca solo el objeto en la base.

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

    // Manda a la página las listas para los dos <select>.
    // Es un método aparte porque lo vamos a reutilizar en guardar y editar.
    private void cargarCatalogos(Model model) {
        model.addAttribute("sexos", sexoRepository.findAll());
        model.addAttribute("obrasSociales", obraSocialRepository.findAll());
    }
}