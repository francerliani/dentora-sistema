package com.dentora.dentora.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    // Nombre real de la persona (ej: "Laura Gómez"), el que se muestra en la barra superior

    @Column(name = "usuario", nullable = false, unique = true, length = 50)
    private String usuario;
    // Nombre de usuario para iniciar sesión (ej: "lgomez"). unique: no se puede repetir

    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;
    // Sin "ñ" en el código para evitar problemas de codificación.
    // length = 100 porque se guarda ENCRIPTADA con BCrypt (unos 60 caracteres),
    // nunca la contraseña real

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    // El atributo nuevo del diagrama. Por defecto, todo usuario nuevo está activo

    // Cantidad de contraseñas incorrectas seguidas. Al llegar a 5, la cuenta se bloquea.
    // "default 0" hace que los usuarios que ya existen en la base arranquen en 0
    @Column(name = "intentos_fallidos", columnDefinition = "integer not null default 0")
    private int intentosFallidos = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
    // @ManyToOne: MUCHOS usuarios pueden tener UN mismo rol (el "* -> 1" del diagrama)
    // @JoinColumn: crea la columna id_rol como clave foránea hacia la tabla rol
    // EAGER: al leer un usuario, trae su rol en la misma consulta.
    //        Lo necesitamos porque en el login hay que saber el rol enseguida

    public Usuario() {
    }

    public Usuario(String nombre, String usuario, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // ===== Getters y setters =====

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
}