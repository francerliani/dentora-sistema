package com.dentora.dentora.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Un tratamiento sobre UNA pieza y una o más de sus caras (RN-18).
// Ej: pieza 22, caras Oclusal + Vestibular, Obturación, A realizar
@Entity
@Table(name = "detalle_tratamiento")
public class DetalleTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @Column(length = 255)
    private String diagnostico;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_consulta", nullable = false)
    private Consulta consulta;

    @ManyToOne
    @JoinColumn(name = "id_diente", nullable = false)
    private Diente diente;

    @ManyToOne
    @JoinColumn(name = "id_tipo_tratamiento", nullable = false)
    private TipoTratamiento tipoTratamiento;

    @ManyToOne
    @JoinColumn(name = "id_estado_tratamiento", nullable = false)
    private EstadoTratamiento estado;

    // MUCHOS A MUCHOS: un detalle afecta varias caras, y una cara aparece en muchos detalles.
    // JPA crea una tabla intermedia "detalle_cara" con (id_detalle, id_cara)
    @ManyToMany
    @JoinTable(name = "detalle_cara",
            joinColumns = @JoinColumn(name = "id_detalle"),
            inverseJoinColumns = @JoinColumn(name = "id_cara"))
    private List<Cara> caras = new ArrayList<>();

    public DetalleTratamiento() {}

    public Integer getIdDetalle() { return idDetalle; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Consulta getConsulta() { return consulta; }
    public void setConsulta(Consulta consulta) { this.consulta = consulta; }
    public Diente getDiente() { return diente; }
    public void setDiente(Diente diente) { this.diente = diente; }
    public TipoTratamiento getTipoTratamiento() { return tipoTratamiento; }
    public void setTipoTratamiento(TipoTratamiento tipoTratamiento) { this.tipoTratamiento = tipoTratamiento; }
    public EstadoTratamiento getEstado() { return estado; }
    public void setEstado(EstadoTratamiento estado) { this.estado = estado; }
    public List<Cara> getCaras() { return caras; }
    public void setCaras(List<Cara> caras) { this.caras = caras; }
}