package com.dentora.dentora.service;

import com.dentora.dentora.model.Disponibilidad;
import com.dentora.dentora.model.FilaAgenda;
import com.dentora.dentora.model.Turno;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.DisponibilidadRepository;
import com.dentora.dentora.repository.EstadoTurnoRepository;
import com.dentora.dentora.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

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
       // =========================================================
    // VALIDAR UN TURNO ANTES DE GUARDARLO (CU-03, pasos 9 a 11)
    // Devuelve el mensaje de error, o null si está todo bien
    // =========================================================
    public String validarTurno(Turno turno) {
        DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter ddmm = DateTimeFormatter.ofPattern("dd/MM");

        // Campos obligatorios
        if (turno.getPaciente() == null)   return "Seleccione un paciente.";
        if (turno.getOdontologo() == null) return "Seleccione un odontólogo.";
        if (turno.getFecha() == null || turno.getHora() == null) return "Complete la fecha y la hora.";

        // Paso 9: paciente activo (RN-05)
        if (!Boolean.TRUE.equals(turno.getPaciente().getActivo())) {
            return "El paciente está dado de baja y no puede recibir turnos.";
        }

        // Paso 9 / 9a: nada en el pasado. Si es HOY, la hora tiene que ser posterior a la actual (NUEVO)
        LocalDate hoy = LocalDate.now();
        boolean fechaPasada = turno.getFecha().isBefore(hoy);
        boolean horaPasada = turno.getFecha().equals(hoy) && !turno.getHora().isAfter(LocalTime.now());
        if (fechaPasada || horaPasada) {
            return "No se pueden asignar turnos en fechas u horas pasadas.";
        }

        LocalTime inicio = turno.getHora();
        LocalTime fin = turno.getHoraFin();

        // Paso 9 / 9a: tiene que entrar COMPLETO en una franja (RN-08)
        List<Disponibilidad> franjas = disponibilidadRepository
                .findByOdontologoAndDiaSemanaOrderByHoraInicio(turno.getOdontologo(), diaEnTexto(turno.getFecha()));

        if (franjas.isEmpty()) {   // 4a: ese día no atiende
            return "El odontólogo no atiende los días «" + nombreDia(turno.getFecha()) + "».";
        }

        // Buscamos la franja donde EMPIEZA el turno
        Disponibilidad franja = null;
        for (Disponibilidad f : franjas) {
            if (!inicio.isBefore(f.getHoraInicio()) && inicio.isBefore(f.getHoraFin())) {
                franja = f;
                break;
            }
        }
        if (franja == null) {
            return "El horario elegido está fuera de la atención del odontólogo.";
        }
        if (fin.isAfter(franja.getHoraFin())) {
            return "El turno de " + turno.getDuracionHoras() + " horas excede el horario de atención del odontólogo (hasta las "
                    + franja.getHoraFin().format(hhmm) + ").";
        }

        // Paso 10 / 10a: no se puede superponer con otro turno del MISMO odontólogo
        for (Turno otro : turnosActivos(turno.getOdontologo(), turno.getFecha())) {
            if (otro.getIdTurno().equals(turno.getIdTurno())) continue;   // al reprogramar, no se compara consigo mismo
            if (inicio.isBefore(otro.getHoraFin()) && otro.getHora().isBefore(fin)) {
                return "El horario de " + inicio.format(hhmm) + " a " + fin.format(hhmm)
                        + " se superpone con otro turno. Elija otro horario o una duración menor.";
            }
        }

        // NUEVO — Paso 11 / 11a: el PACIENTE no puede tener otro turno Pendiente a la misma hora
        // (aunque sea con otro odontólogo)
        for (Turno otro : turnoRepository.findByPacienteAndFechaAndEstado_Nombre(
                turno.getPaciente(), turno.getFecha(), "Pendiente")) {
            if (otro.getIdTurno().equals(turno.getIdTurno())) continue;
            if (inicio.isBefore(otro.getHoraFin()) && otro.getHora().isBefore(fin)) {
                return "El paciente ya tiene un turno el " + otro.getFecha().format(ddmm) + " a las "
                        + otro.getHora().format(hhmm) + " con «" + otro.getOdontologo().getNombre() + "».";
            }
        }

        return null;   // todo bien
    }

    // "LUNES" → "Lunes", "MIERCOLES" → "Miércoles" (para los mensajes)
    public String nombreDia(LocalDate fecha) {
        String dia = diaEnTexto(fecha);
        return switch (dia) {
            case "MIERCOLES" -> "Miércoles";
            case "SABADO"    -> "Sábado";
            default          -> dia.charAt(0) + dia.substring(1).toLowerCase();
        };
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

        // =========================================================
    // NUEVO: buscar un turno por id (para reprogramar)
    // =========================================================
    public Turno buscarTurno(Integer idTurno) {
        return turnoRepository.findById(idTurno).orElse(null);
    }

    // =========================================================
    // NUEVO: CANCELAR (no se borra: cambia el estado, RN-10)
    // Solo se cancelan los Pendientes. Devuelve el turno para saber a qué agenda volver
    // =========================================================
    public Turno cancelarTurno(Integer idTurno) {
        Turno turno = turnoRepository.findById(idTurno).orElse(null);

        if (turno != null && turno.getEstado().getNombre().equals("Pendiente")) {
            turno.setEstado(estadoTurnoRepository.findByNombre("Cancelado").orElseThrow());
            turnoRepository.save(turno);
        }
        return turno;
    }

        // =========================================================
    // NUEVO: MARCAR AUSENTE (el paciente no vino)
    // Solo turnos Pendientes que ya empezaron. Devuelve el mensaje de error, o null si salió bien
    // =========================================================
    public String marcarAusente(Integer idTurno) {
        Turno turno = turnoRepository.findById(idTurno).orElse(null);
        if (turno == null) {
            return "El turno no existe.";
        }
        if (!turno.getEstado().getNombre().equals("Pendiente")) {
            return "Solo se pueden marcar como ausentes los turnos pendientes.";
        }
        if (!turno.isComenzado()) {
            return "Todavía no es la hora del turno: no se puede marcar como ausente.";
        }
        turno.setEstado(estadoTurnoRepository.findByNombre("Ausente").orElseThrow());
        turnoRepository.save(turno);
        return null;
    }
}