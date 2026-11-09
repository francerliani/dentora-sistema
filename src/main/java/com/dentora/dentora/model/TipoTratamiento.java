package com.dentora.dentora.model;

import jakarta.persistence.*;

// Catálogo: Obturación, Extracción, Endodoncia... (lo administra el Administrador)
@Entity
@Table(name = "tipo_tratamiento")
public class TipoTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_tratamiento")
    private Integer idTipoTrat;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    public TipoTratamiento() {}
    public TipoTratamiento(String nombre) { this.nombre = nombre; }

    public Integer getIdTipoTrat() { return idTipoTrat; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}