package com.dentora.dentora.model;

// Lo que llega del formulario de usuario. NO es una tabla.
// Se usa una clase aparte (y no Usuario directamente) porque tiene
// "repetirContrasena", que no existe en la base, y la contraseña llega sin encriptar.
public class UsuarioForm {

    private Integer idUsuario;           // null = usuario nuevo
    private String nombre;
    private String usuario;
    private String contrasena;
    private String repetirContrasena;
    private Integer idRol;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRepetirContrasena() { return repetirContrasena; }
    public void setRepetirContrasena(String repetirContrasena) { this.repetirContrasena = repetirContrasena; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
}