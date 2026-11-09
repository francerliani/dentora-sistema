package com.dentora.dentora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Una visita del paciente al odontólogo. Una vez guardada NO se modifica (RN-16)
@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer idConsulta;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 255)
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    // El odontólogo que registró la consulta (el usuario en sesión)
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario odontologo;

    // Turno del que surgió (puede ser null: consulta espontánea)
    @OneToOne
    @JoinColumn(name = "id_turno")
    private Turno turno;

    // COMPOSICIÓN: los detalles son parte de la consulta.
    // cascade = ALL → al guardar la consulta se guardan todos sus detalles juntos.
    // mappedBy = "consulta" → la columna id_consulta está en la tabla detalle_tratamiento
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    private List<DetalleTratamiento> detalles = new ArrayList<>();

    public Consulta() {}

    // Agrega un detalle y le avisa a qué consulta pertenece (las dos puntas de la relación)
    public void agregarDetalle(DetalleTratamiento detalle) {
        detalle.setConsulta(this);
        detalles.add(detalle);
    }

    public Integer getIdConsulta() { return idConsulta; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Usuario getOdontologo() { return odontologo; }
    public void setOdontologo(Usuario odontologo) { this.odontologo = odontologo; }
    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }
    public List<DetalleTratamiento> getDetalles() { return detalles; }
}