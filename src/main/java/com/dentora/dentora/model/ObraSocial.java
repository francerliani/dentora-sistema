package com.dentora.dentora.model;


import jakarta.persistence.*;
// jakarta.persistence = las anotaciones de JPA (@Entity, @Id, @Column, etc.)

@Entity
// @Entity: esta clase representa una tabla de la base de datos

@Table(name = "obra_social")
// @Table: nombre de la tabla en PostgreSQL. Sin esto, usaría el nombre de la clase

public class ObraSocial {

    @Id
    // @Id: este atributo es la clave primaria (PK) de la tabla

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: PostgreSQL genera el número solo (1, 2, 3...), como un SERIAL
    @Column(name = "id_obra_social")
    private Integer idObraSocial;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    // nullable = false -> NOT NULL (obligatorio)
    // unique = true    -> no puede haber dos Obras sociales con el mismo nombre
    // length = 100      -> VARCHAR(100)
    private String nombre;

    // Constructor vacío: JPA lo NECESITA para crear objetos al leer de la base
    public ObraSocial() {
    }

    // Constructor práctico para crear Obra Social desde el código
    public ObraSocial(String nombre) {
        this.nombre = nombre;
    }

    // Getters y setters: JPA y Spring los usan para leer y escribir los atributos
    public Integer getIdObraSocial() {
        return idObraSocial;
    }

    public void setIdObraSocial(Integer idObraSocial) {
        this.idObraSocial = idObraSocial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}