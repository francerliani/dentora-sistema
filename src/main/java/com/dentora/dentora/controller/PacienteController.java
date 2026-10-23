package com.dentora.dentora.controller;

import com.dentora.dentora.repository.PacienteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    // Spring nos pasa el repositorio automáticamente
    public PacienteController(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    // Cuando alguien entra a /pacientes...
    @GetMapping("/pacientes")
public String listar(@RequestParam(required = false) String buscar, Model model) {

    if (buscar == null || buscar.isBlank()) {
        // No escribió nada: muestra todos los activos
        model.addAttribute("pacientes", pacienteRepository.findByActivoTrue());
    } else {
        // Escribió algo: filtra
        model.addAttribute("pacientes", pacienteRepository.buscar(buscar.trim()));
    }

    // Devolvemos el texto para que quede escrito en el buscador
    model.addAttribute("buscar", buscar);
    return "pacientes";
}

}

