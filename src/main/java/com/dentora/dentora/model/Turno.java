package com.dentora.dentora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer idTurno;

    // LocalDate = solo la fecha (sin hora)
    @Column(nullable = false)
    private LocalDate fecha;

    // Hora de inicio del turno (siempre en punto)
    @Column(nullable = false)
    private LocalTime hora;

    // Cuántos bloques de 1 hora ocupa: 1 por defecto, 2 a 4 para tratamientos largos (RN-08)
    @Column(name = "duracion_horas", nullable = false)
    private int duracionHoras = 1;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    // El odontólogo que atiende el turno
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario odontologo;

    @ManyToOne
    @JoinColumn(name = "id_estado_turno", nullable = false)
    private EstadoTurno estado;

    public Turno() {}

    // Hora en que termina el turno. Ej: 16:00 + 2 horas = 18:00.
    // No se guarda en la base: se calcula (por eso no tiene @Column)
    public LocalTime getHoraFin() {
        return hora.plusHours(duracionHoras);
    }

    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public int getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(int duracionHoras) { this.duracionHoras = duracionHoras; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Usuario getOdontologo() { return odontologo; }
    public void setOdontologo(Usuario odontologo) { this.odontologo = odontologo; }
    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }
}