package com.dentora.dentora.model;

import jakarta.persistence.*;

// Catálogo fijo: "A realizar" (rojo) y "Realizado" (azul)
@Entity
@Table(name = "estado_tratamiento")
public class EstadoTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_tratamiento")
    private Integer idEstadoTrat;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public EstadoTratamiento() {}
    public EstadoTratamiento(String nombre) { this.nombre = nombre; }

    public Integer getIdEstadoTrat() { return idEstadoTrat; }
    public String getNombre() { return nombre; }
}