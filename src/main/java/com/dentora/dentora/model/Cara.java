package com.dentora.dentora.model;

import jakarta.persistence.*;

// Catálogo fijo: Oclusal, Vestibular, Palatina, Lingual, Mesial, Distal (RN-18)
@Entity
@Table(name = "cara")
public class Cara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cara")
    private Integer idCara;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Cara() {}
    public Cara(String nombre) { this.nombre = nombre; }

    public Integer getIdCara() { return idCara; }
    public String getNombre() { return nombre; }
}