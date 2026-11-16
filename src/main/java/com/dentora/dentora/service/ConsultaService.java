package com.dentora.dentora.service;

import com.dentora.dentora.model.*;
import com.dentora.dentora.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// Reglas de negocio para registrar una consulta (CU-05, pasos 15 a 17)
@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final TurnoRepository turnoRepository;
    private final DienteRepository dienteRepository;
    private final CaraRepository caraRepository;
    private final TipoTratamientoRepository tipoTratamientoRepository;
    private final EstadoTratamientoRepository estadoTratamientoRepository;
    private final EstadoTurnoRepository estadoTurnoRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           PacienteRepository pacienteRepository,
                           TurnoRepository turnoRepository,
                           DienteRepository dienteRepository,
                           CaraRepository caraRepository,
                           TipoTratamientoRepository tipoTratamientoRepository,
                           EstadoTratamientoRepository estadoTratamientoRepository,
                           EstadoTurnoRepository estadoTurnoRepository) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.turnoRepository = turnoRepository;
        this.dienteRepository = dienteRepository;
        this.caraRepository = caraRepository;
        this.tipoTratamientoRepository = tipoTratamientoRepository;
        this.estadoTratamientoRepository = estadoTratamientoRepository;
        this.estadoTurnoRepository = estadoTurnoRepository;
    }

    // @Transactional = TODO o NADA (paso 16 y alternativo 16a):
    // si algo falla en el medio, se deshace todo y no queda una consulta a medias
    @Transactional
    public void registrar(ConsultaForm form, Usuario odontologo) {

        // ===== Validaciones de la consulta (paso 15) =====
        if (form.getMotivo() == null || form.getMotivo().isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }
        if (form.getMotivo().length() > 255) {
            throw new IllegalArgumentException("El motivo puede tener hasta 255 caracteres.");
        }
        if (form.getFecha() == null || form.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura.");
        }

        Paciente paciente = pacienteRepository.findById(form.getIdPaciente())
                .orElseThrow(() -> new IllegalArgumentException("El paciente no existe."));

        // ===== Armar la consulta =====
        Consulta consulta = new Consulta();
        consulta.setFecha(form.getFecha());
        consulta.setMotivo(form.getMotivo().trim());
        consulta.setPaciente(paciente);
        consulta.setOdontologo(odontologo);   // el usuario en sesión

        // ===== Un DetalleTratamiento por cada tratamiento agregado =====
        // (RN-17: si no hay ninguno, la consulta se guarda igual)
        for (DetalleForm df : form.getDetalles()) {
            if (df.getDiente() == null) continue;   // fila vacía: se ignora

            // Validaciones del detalle (paso 12)
                        // Paso 12: el diagnóstico también es obligatorio (devolución de los profesores)
            if (df.getCaras() == null || df.getCaras().isBlank()
                    || df.getIdTipo() == null || df.getEstado() == null
                    || vacioANull(df.getDiagnostico()) == null) {
                throw new IllegalArgumentException(
                        "Seleccioná al menos una cara y completá tratamiento, estado y diagnóstico.");
            }
            if (df.getDiagnostico() != null && df.getDiagnostico().length() > 255) {
                throw new IllegalArgumentException("El diagnóstico puede tener hasta 255 caracteres.");
            }
            if (df.getObservaciones() != null && df.getObservaciones().length() > 500) {
                throw new IllegalArgumentException("Las observaciones pueden tener hasta 500 caracteres.");
            }

            DetalleTratamiento d = new DetalleTratamiento();
            d.setDiente(dienteRepository.findByNumeroFDI(df.getDiente())
                    .orElseThrow(() -> new IllegalArgumentException("Pieza inexistente: " + df.getDiente())));
            d.setTipoTratamiento(tipoTratamientoRepository.findById(df.getIdTipo())
                    .orElseThrow(() -> new IllegalArgumentException("Tratamiento inexistente.")));

            // "realizado" → "Realizado"; cualquier otro → "A realizar"
            String nombreEstado = df.getEstado().equals("realizado") ? "Realizado" : "A realizar";
            d.setEstado(estadoTratamientoRepository.findByNombre(nombreEstado).orElseThrow());

            // "oclusal,vestibular" → busca cada Cara en la base ("Oclusal", "Vestibular")
            for (String texto : df.getCaras().split(",")) {
                String nombre = texto.trim();
                nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();
                String nombreFinal = nombre;
                d.getCaras().add(caraRepository.findByNombre(nombre)
                        .orElseThrow(() -> new IllegalArgumentException("Cara inexistente: " + nombreFinal)));
            }

            // Campos opcionales: si vienen vacíos se guardan como null
            d.setDiagnostico(vacioANull(df.getDiagnostico()));
            d.setObservaciones(vacioANull(df.getObservaciones()));

            consulta.agregarDetalle(d);
        }

        // ===== Turno asociado → pasa a Atendido (paso 17, RN-09) =====
        if (form.getIdTurno() != null) {
            Turno turno = turnoRepository.findById(form.getIdTurno())
                    .orElseThrow(() -> new IllegalArgumentException("El turno no existe."));
            consulta.setTurno(turno);
            turno.setEstado(estadoTurnoRepository.findByNombre("Atendido").orElseThrow());
            // No hace falta save(turno): dentro de @Transactional, JPA guarda solo los cambios
        }

        // Un solo save: por el cascade también se guardan todos los detalles
        consultaRepository.save(consulta);
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}