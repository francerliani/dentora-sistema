package com.dentora.dentora.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Lo que llega del formulario "Registrar consulta". NO es una tabla.
// Spring llena cada atributo con el campo del mismo nombre del formulario.
public class ConsultaForm {

    private Integer idPaciente;
    private LocalDate fecha;
    private String motivo;
    private Integer idTurno;   // null = sin turno (consulta espontánea)

    // Los tratamientos agregados. Llegan como detalles[0].diente, detalles[0].caras, etc.
    private List<DetalleForm> detalles = new ArrayList<>();

    public Integer getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Integer idPaciente) { this.idPaciente = idPaciente; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Integer getIdTurno() { return idTurno; }
    public void setIdTurno(Integer idTurno) { this.idTurno = idTurno; }
    public List<DetalleForm> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleForm> detalles) { this.detalles = detalles; }
}