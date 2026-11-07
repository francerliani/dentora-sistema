package com.dentora.dentora.service;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.FilaAgenda;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.DisponibilidadRepository;
import com.dentora.dentora.repository.EstadoTurnoRepository;
import com.dentora.dentora.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Lógica de la agenda y reglas de negocio de los turnos
@Service
public class AgendaService {

    private final TurnoRepository turnoRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final EstadoTurnoRepository estadoTurnoRepository;   // NUEVO: para poner "Pendiente"

    public AgendaService(TurnoRepository turnoRepository,
                         DisponibilidadRepository disponibilidadRepository,
                         EstadoTurnoRepository estadoTurnoRepository) {
        this.turnoRepository = turnoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.estadoTurnoRepository = estadoTurnoRepository;
    }

    // =========================================================
    // ARMAR LA AGENDA (igual que antes)
    // =========================================================
    public List<FilaAgenda> armarAgenda(Usuario odontologo, LocalDate fecha) {
        List<FilaAgenda> filas = new ArrayList<>();

        List<Disponibilidad> franjas = disponibilidadRepository
                .findByOdontologoAndDiaSemanaOrderByHoraInicio(odontologo, diaEnTexto(fecha));

        List<Turno> turnos = turnosActivos(odontologo, fecha);

        for (Disponibilidad franja : franjas) {
            LocalTime hora = franja.getHoraInicio();
            while (hora.isBefore(franja.getHoraFin())) {
                Turno turno = turnoQueEmpiezaA(turnos, hora);
                if (turno != null) {
                    filas.add(new FilaAgenda(hora, turno));
                    hora = turno.getHoraFin();
                } else {
                    filas.add(new FilaAgenda(hora, null));
                    hora = hora.plusHours(1);
                }
            }
        }
        return filas;
    }

    // =========================================================
    // NUEVO: VALIDAR UN TURNO ANTES DE GUARDARLO
    // Devuelve el mensaje de error, o null si está todo bien
    // =========================================================
    public String validarTurno(Turno turno) {

        // 1) Campos obligatorios
        if (turno.getPaciente() == null)   return "Seleccione un paciente.";
        if (turno.getOdontologo() == null) return "Seleccione un odontólogo.";
        if (turno.getFecha() == null || turno.getHora() == null) return "Complete la fecha y la hora.";

        // 2) Paciente activo (RN-05)
        if (!turno.getPaciente().getActivo()) {
            return "El paciente está dado de baja y no puede recibir turnos.";
        }

        // 3) Nada de fechas pasadas
        if (turno.getFecha().isBefore(LocalDate.now())) {
            return "No se pueden dar turnos en fechas pasadas.";
        }

        LocalTime inicio = turno.getHora();
        LocalTime fin = turno.getHoraFin();   // inicio + duración

        // 4) Tiene que entrar COMPLETO dentro de una franja (RN-08)
        boolean entraEnUnaFranja = false;
        List<Disponibilidad> franjas = disponibilidadRepository
                .findByOdontologoAndDiaSemanaOrderByHoraInicio(turno.getOdontologo(), diaEnTexto(turno.getFecha()));

        for (Disponibilidad franja : franjas) {
            // "no empieza antes del inicio de la franja Y no termina después del fin"
            if (!inicio.isBefore(franja.getHoraInicio()) && !fin.isAfter(franja.getHoraFin())) {
                entraEnUnaFranja = true;
                break;
            }
        }
        if (!entraEnUnaFranja) {
            return "El turno no entra en el horario de atención del odontólogo ese día.";
        }

        // 5) No se puede superponer con otro turno (los cancelados no cuentan)
        for (Turno otro : turnosActivos(turno.getOdontologo(), turno.getFecha())) {

            // Al reprogramar, el turno no se compara consigo mismo
            if (otro.getIdTurno().equals(turno.getIdTurno())) continue;

            // Dos turnos se pisan si uno empieza antes de que el otro termine, y viceversa
            if (inicio.isBefore(otro.getHoraFin()) && otro.getHora().isBefore(fin)) {
                return "El horario se superpone con el turno de "
                        + otro.getPaciente().getApellido() + " (" + otro.getHora()
                        + " a " + otro.getHoraFin() + ").";
            }
        }

        return null;   // todo bien
    }

    // =========================================================
    // NUEVO: GUARDAR un turno nuevo (siempre arranca Pendiente, RN-09)
    // =========================================================
    public void guardarTurno(Turno turno) {
        turno.setEstado(estadoTurnoRepository.findByNombre("Pendiente").orElseThrow());
        turnoRepository.save(turno);
    }

    // ===== Métodos auxiliares =====

    // Turnos de un odontólogo en una fecha, SIN los cancelados
    private List<Turno> turnosActivos(Usuario odontologo, LocalDate fecha) {
        return turnoRepository.findByOdontologoAndFechaOrderByHora(odontologo, fecha)
                .stream()
                .filter(t -> !t.getEstado().getNombre().equals("Cancelado"))
                .toList();
    }

    private Turno turnoQueEmpiezaA(List<Turno> turnos, LocalTime hora) {
        for (Turno t : turnos) {
            if (t.getHora().equals(hora)) {
                return t;
            }
        }
        return null;
    }

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