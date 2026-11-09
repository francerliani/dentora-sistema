package com.dentora.dentora.model;

// Un tratamiento tal como llega del formulario. NO es una tabla.
public class DetalleForm {

    private Integer diente;        // número FDI, ej: 22
    private String caras;          // ej: "oclusal,vestibular"
    private Integer idTipo;        // id del TipoTratamiento
    private String estado;         // "a-realizar" o "realizado"
    private String diagnostico;
    private String observaciones;

    public Integer getDiente() { return diente; }
    public void setDiente(Integer diente) { this.diente = diente; }
    public String getCaras() { return caras; }
    public void setCaras(String caras) { this.caras = caras; }
    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}