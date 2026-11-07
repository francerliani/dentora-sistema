package com.dentora.dentora.model;

import java.time.LocalTime;

// Un renglón de la agenda: un bloque libre o un turno.
// NO es una tabla (no tiene @Entity): solo sirve para mostrar la agenda.
public class FilaAgenda {

    private final LocalTime hora;  // hora de inicio del renglón
    private final Turno turno;     // null = bloque libre

    public FilaAgenda(LocalTime hora, Turno turno) {
        this.hora = hora;
        this.turno = turno;
    }

    public LocalTime getHora() { return hora; }
    public Turno getTurno() { return turno; }

    // ¿El bloque está libre?
    public boolean isLibre() { return turno == null; }

    // Hora de fin: si es un turno, según su duración; si está libre, 1 hora
    public LocalTime getHoraFin() {
        return (turno != null) ? turno.getHoraFin() : hora.plusHours(1);
    }
}