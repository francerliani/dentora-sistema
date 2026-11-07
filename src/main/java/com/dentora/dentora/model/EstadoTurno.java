package com.dentora.dentora.model;

import jakarta.persistence.*;

// Catálogo fijo (RN-13): sus valores se cargan al iniciar y no se editan
@Entity
@Table(name = "estado_turno")
public class EstadoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_turno")
    private Integer idEstadoTurno;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public EstadoTurno() {}

    public EstadoTurno(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdEstadoTurno() { return idEstadoTurno; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}