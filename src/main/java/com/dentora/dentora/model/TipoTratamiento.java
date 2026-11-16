package com.dentora.dentora.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
// Catálogo: Obturación, Extracción, Endodoncia... (lo administra el Administrador)
@Entity
@Table(name = "tipo_tratamiento")
public class TipoTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_tratamiento")
    private Integer idTipoTrat;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    public TipoTratamiento() {}
    public TipoTratamiento(String nombre) { this.nombre = nombre; }
    // Precio de la prestación. BigDecimal (y no double) porque es dinero:
    // double tiene errores de redondeo (0.1 + 0.2 = 0.30000000000000004)
    // "default 0" hace que los tratamientos que ya existen arranquen en $0
    @Column(columnDefinition = "numeric(12,2) not null default 0")
    private BigDecimal precio = BigDecimal.ZERO;

    public Integer getIdTipoTrat() { return idTipoTrat; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}