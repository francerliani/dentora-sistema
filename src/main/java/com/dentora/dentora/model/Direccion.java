package com.dentora.dentora.model;
// "package" indica en qué carpeta está la clase. Tiene que coincidir con la ubicación real

import jakarta.persistence.*;
// jakarta.persistence = las anotaciones de JPA (@Entity, @Id, @Column, etc.)

@Entity
// @Entity: esta clase representa una tabla de la base de datos

@Table(name = "direccion")
// @Table: nombre de la tabla en PostgreSQL. Sin esto, usaría el nombre de la clase
public class Direccion {

    @Id
    // @Id: este atributo es la clave primaria (PK) de la tabla

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: PostgreSQL genera el número solo (1, 2, 3...), como un SERIAL
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @Column(name = "calle", length = 100)
    // opcional, VARCHAR(100).
    // length = 100      -> VARCHAR(100)
    private String calle;

    @Column(name = "numero")
    
    private Integer numero;

    @Column(name = "ciudad", length = 100)
    
    private String ciudad;


    //constructores
    // Constructor vacío: JPA lo necesita para crear objetos al leer de la base
public Direccion() {
}

// Constructor práctico para crear direcciones desde el código
public Direccion(String calle, Integer numero, String ciudad) {
    this.calle = calle;
    this.numero = numero;
    this.ciudad = ciudad;
}




    //getters y setters

    public Integer getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }


    
}