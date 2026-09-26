package com.dentora.dentora.model;
// "package" indica en qué carpeta está la clase. Tiene que coincidir con la ubicación real

import jakarta.persistence.*;
// jakarta.persistence = las anotaciones de JPA (@Entity, @Id, @Column, etc.)

@Entity
// @Entity: esta clase representa una tabla de la base de datos

@Table(name = "sexo")
// @Table: nombre de la tabla en PostgreSQL. Sin esto, usaría el nombre de la clase
public class Sexo {

    @Id
    // @Id: este atributo es la clave primaria (PK) de la tabla

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: PostgreSQL genera el número solo (1, 2, 3...), como un SERIAL
    @Column(name = "id_sexo")
    private Integer idSexo;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    // nullable = false -> NOT NULL (obligatorio)
    // unique = true    -> no puede haber dos sexos con el mismo nombre
    // length = 30      -> VARCHAR(30)
    private String nombre;

    // Constructor vacío: JPA lo NECESITA para crear objetos al leer de la base
    public Sexo() {
    }

    // Constructor práctico para crear sexos desde el código
    public Sexo(String nombre) {
        this.nombre = nombre;
    }

    // Getters y setters: JPA y Spring los usan para leer y escribir los atributos
    public Integer getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(Integer idSexo) {
        this.idSexo = idSexo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}