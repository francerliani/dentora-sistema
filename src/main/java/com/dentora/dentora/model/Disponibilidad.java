package com.dentora.dentora.model;

import jakarta.persistence.*;
import java.time.LocalTime;

// Una franja de atención de un odontólogo. Ej: jgaite, LUNES, de 15:00 a 20:00.
// Un odontólogo puede tener varias franjas (una por día, o dos el mismo día).
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Integer idDisponibilidad;

    // Día de la semana en mayúsculas y sin tilde: LUNES, MARTES, MIERCOLES...
    @Column(name = "dia_semana", nullable = false, length = 15)
    private String diaSemana;

    // LocalTime = solo la hora (sin fecha). Siempre en punto: 15:00, no 15:30 (RN-12)
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    // El odontólogo dueño de esta franja (es un Usuario con rol ODONTOLOGO)
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario odontologo;

    public Disponibilidad() {}

    public Disponibilidad(String diaSemana, LocalTime horaInicio, LocalTime horaFin, Usuario odontologo) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.odontologo = odontologo;
    }

    public Integer getIdDisponibilidad() { return idDisponibilidad; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public Usuario getOdontologo() { return odontologo; }
    public void setOdontologo(Usuario odontologo) { this.odontologo = odontologo; }
}