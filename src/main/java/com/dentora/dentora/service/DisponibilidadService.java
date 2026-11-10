package com.dentora.dentora.service;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.DisponibilidadRepository;
import com.dentora.dentora.repository.TurnoRepository;
import com.dentora.dentora.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

// Reglas de la pestaña Disponibilidad (CU-04, sub-flujo D)
@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final AgendaService agendaService;   // reutilizamos su diaEnTexto()

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                 UsuarioRepository usuarioRepository,
                                 TurnoRepository turnoRepository,
                                 AgendaService agendaService) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.agendaService = agendaService;
    }

    // =========================================================
    // AGREGAR una franja (D3 a D6)
    // =========================================================
    public void agregar(Integer idOdontologo, String diaSemana, LocalTime inicio, LocalTime fin) {

        // D4 / D4a: datos completos y válidos
        if (diaSemana == null || diaSemana.isBlank() || inicio == null || fin == null) {
            throw new IllegalArgumentException("Complete el día y las dos horas.");
        }
        if (inicio.getMinute() != 0 || fin.getMinute() != 0) {
            throw new IllegalArgumentException("Las horas deben ser en punto (por ejemplo 09:00).");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }

        Usuario odontologo = usuarioRepository.findById(idOdontologo)
                .orElseThrow(() -> new IllegalArgumentException("El odontólogo no existe."));

        // D5 / D5a: no puede pisarse con otra franja del mismo día
        // (misma regla que los turnos: se pisan si una empieza antes de que la otra termine, y viceversa)
        for (Disponibilidad otra : disponibilidadRepository
                .findByOdontologoAndDiaSemanaOrderByHoraInicio(odontologo, diaSemana)) {
            if (inicio.isBefore(otra.getHoraFin()) && otra.getHoraInicio().isBefore(fin)) {
                throw new IllegalArgumentException("La franja se superpone con el horario «"
                        + nombreDia(diaSemana) + "» de " + otra.getHoraInicio() + " a " + otra.getHoraFin() + ".");
            }
        }

        // D6
        disponibilidadRepository.save(new Disponibilidad(diaSemana, inicio, fin, odontologo));
    }

    // =========================================================
    // QUITAR una franja (D2a). Devuelve cuántos turnos pendientes futuros
    // había dentro de ella (no se tocan: se conservan, RN-10)
    // =========================================================
    public long quitar(Integer idDisponibilidad) {
        Disponibilidad franja = disponibilidadRepository.findById(idDisponibilidad)
                .orElseThrow(() -> new IllegalArgumentException("La franja no existe."));

        // Contamos los turnos Pendientes desde hoy que caen en ese día y dentro de ese horario
        long turnosEnLaFranja = turnoRepository
                .findByOdontologoAndEstado_NombreAndFechaGreaterThanEqual(franja.getOdontologo(), "Pendiente", LocalDate.now())
                .stream()
                .filter(t -> agendaService.diaEnTexto(t.getFecha()).equals(franja.getDiaSemana()))
                .filter(t -> !t.getHora().isBefore(franja.getHoraInicio()) && t.getHora().isBefore(franja.getHoraFin()))
                .count();

        disponibilidadRepository.delete(franja);
        return turnosEnLaFranja;
    }

    // "MIERCOLES" → "Miércoles" (para los mensajes)
    private String nombreDia(String dia) {
        return switch (dia) {
            case "MIERCOLES" -> "Miércoles";
            case "SABADO" -> "Sábado";
            default -> dia.charAt(0) + dia.substring(1).toLowerCase();
        };
    }
}