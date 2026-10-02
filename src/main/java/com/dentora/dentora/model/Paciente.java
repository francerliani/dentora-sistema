package com.dentora.dentora.model;

import java.time.LocalDate;
import jakarta.persistence.*; // jakarta.persistence = las anotaciones de JPA (@Entity, @Id, @Column, @ManyToOne, etc.)

@Entity  // @Entity: esta clase representa una tabla de la base de datos

@Table(name = "paciente")
// @Table: nombre de la tabla en PostgreSQL
public class Paciente {

    // =========================================================
    // CLAVE PRIMARIA
    // =========================================================

    @Id
    // @Id: este atributo es la clave primaria (PK) de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: PostgreSQL genera el número solo (1, 2, 3...)
    @Column(name = "id_paciente")
    private Integer idPaciente;

    // =========================================================
    // ATRIBUTOS SIMPLES (cada uno con su propio @Column arriba)
    // =========================================================

    @Column(name = "nombre", nullable = false, length = 100)
    // Obligatorio (NOT NULL), hasta 100 caracteres
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    // Obligatorio, hasta 100 caracteres
    private String apellido;

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    // Obligatorio y ÚNICO: no puede haber dos pacientes con el mismo DNI.
    // Es String y no Integer porque es un IDENTIFICADOR, no un número para hacer cuentas:
    // nunca vas a sumar ni restar DNIs, y como texto se respetan ceros adelante y formatos
    private String dni;

    @Column(name = "fecha_nacimiento")
    // Opcional: no lleva nullable = false.
    // En Java el atributo va en camelCase (fechaNacimiento);
    // en la base, la columna va con guion bajo (fecha_nacimiento)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", length = 30)
    // Opcional, hasta 30 caracteres. String porque puede tener espacios o guiones: "342 555-1234"
    private String telefono;

    @Column(name = "activo", nullable = false)
    // Obligatorio. Permite la BAJA LÓGICA: "dar de baja" a un paciente no lo borra,
    // solo pone activo = false. Así se conserva su historia clínica
    private Boolean activo = true;
    // = true: todo paciente nuevo empieza activo

    // =========================================================
    // RELACIONES (las flechas del diagrama de clases)
    // =========================================================

    @ManyToOne
    // MUCHOS pacientes pueden tener UN mismo sexo ("Paciente * -> 1 Sexo" en el diagrama)
    @JoinColumn(name = "id_sexo")
    // Crea la columna id_sexo como CLAVE FORÁNEA hacia la tabla sexo.
    // Sin nullable = false: es opcional, se puede dejar sin elegir
    private Sexo sexo;

    @ManyToOne
    // MUCHOS pacientes pueden tener UNA misma obra social (ej: muchos de OSDE)
    @JoinColumn(name = "id_obra_social")
    // Opcional: si queda vacía (null), el paciente es "Particular" (sin obra social)
    private ObraSocial obraSocial;

    @ManyToOne(cascade = CascadeType.ALL)
    // cascade = ALL: al GUARDAR un paciente, se guarda AUTOMÁTICAMENTE su dirección.
    // Sin esto, habría que guardar primero la dirección por separado y después el paciente.
    // Sexo y ObraSocial NO llevan cascade porque ya existen en sus catálogos:
    // no se crean junto con el paciente, solo se eligen de la lista
    @JoinColumn(name = "id_direccion")
    // Opcional: la dirección se puede dejar vacía
    private Direccion direccion;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    // Constructor vacío: JPA lo NECESITA para crear objetos al leer de la base.
    // No hace falta uno con parámetros: los datos van a llegar desde el formulario
    public Paciente() {
    }

    // =========================================================
    // GETTERS Y SETTERS
    // JPA, Spring y Thymeleaf los usan para leer y escribir los atributos.
    // Los nombres tienen que ser EXACTOS: get/set + nombre del atributo con mayúscula
    // =========================================================

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public ObraSocial getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(ObraSocial obraSocial) {
        this.obraSocial = obraSocial;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}