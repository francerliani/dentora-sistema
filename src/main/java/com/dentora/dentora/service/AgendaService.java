package com.dentora.dentora.service;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.FilaAgenda;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.DisponibilidadRepository;
import com.dentora.dentora.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Lógica de la agenda: combina las franjas del odontólogo con sus turnos
@Service
public class AgendaService {

    private final TurnoRepository turnoRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    public AgendaService(TurnoRepository turnoRepository,
                         DisponibilidadRepository disponibilidadRepository) {
        this.turnoRepository = turnoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
    }

    // Arma la agenda de un odontólogo en una fecha, en bloques de 1 hora (RN-08)
    public List<FilaAgenda> armarAgenda(Usuario odontologo, LocalDate fecha) {
        List<FilaAgenda> filas = new ArrayList<>();

        // 1) Las franjas de ese día de la semana (ej: LUNES 15:00-20:00)
        List<Disponibilidad> franjas = disponibilidadRepository
                .findByOdontologoAndDiaSemanaOrderByHoraInicio(odontologo, diaEnTexto(fecha));

        // 2) Los turnos de esa fecha, sin los cancelados (un cancelado libera sus bloques, RN-09)
        List<Turno> turnos = turnoRepository.findByOdontologoAndFechaOrderByHora(odontologo, fecha)
                .stream()
                .filter(t -> !t.getEstado().getNombre().equals("Cancelado"))
                .toList();

        // 3) Recorremos cada franja hora por hora
        for (Disponibilidad franja : franjas) {
            LocalTime hora = franja.getHoraInicio();

            while (hora.isBefore(franja.getHoraFin())) {
                Turno turno = turnoQueEmpiezaA(turnos, hora);

                if (turno != null) {
                    // Hay un turno: una fila, y saltamos todas las horas que ocupa
                    filas.add(new FilaAgenda(hora, turno));
                    hora = turno.getHoraFin();
                } else {
                    // Bloque libre: una fila y pasamos a la hora siguiente
                    filas.add(new FilaAgenda(hora, null));
                    hora = hora.plusHours(1);
                }
            }
        }
        return filas;
    }

    // Busca en la lista el turno que empieza a esa hora (o null si no hay)
    private Turno turnoQueEmpiezaA(List<Turno> turnos, LocalTime hora) {
        for (Turno t : turnos) {
            if (t.getHora().equals(hora)) {
                return t;
            }
        }
        return null;
    }

    // Convierte una fecha al texto que usamos en Disponibilidad (LUNES, MARTES...)
    public String diaEnTexto(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY    -> "LUNES";
            case TUESDAY   -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY  -> "JUEVES";
            case FRIDAY    -> "VIERNES";
            case SATURDAY  -> "SABADO";
            case SUNDAY    -> "DOMINGO";
        };
    }
}