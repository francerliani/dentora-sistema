package com.dentora.dentora.model;

import jakarta.persistence.*;

// Catálogo fijo: las 32 piezas permanentes en numeración FDI (11 a 48)
@Entity
@Table(name = "diente")
public class Diente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diente")
    private Integer idDiente;

    // FDI: primer dígito = cuadrante (1 a 4), segundo = pieza (1 a 8). Ej: 22
    @Column(name = "numero_fdi", nullable = false, unique = true)
    private int numeroFDI;

    public Diente() {}
    public Diente(int numeroFDI) { this.numeroFDI = numeroFDI; }

    public Integer getIdDiente() { return idDiente; }
    public int getNumeroFDI() { return numeroFDI; }
}