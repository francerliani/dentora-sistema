package com.dentora.dentora.model;

import java.math.BigDecimal;

// Un renglón de un reporte: "Juan Gaite — 5 turnos" o "OSDE — 12 pacientes" o "Juan Gaite — $1.500.000"
// NO es una tabla: se arma en el momento a partir de los datos
public class FilaReporte {

    private final String etiqueta;          // nombre del odontólogo u obra social
    private long cantidad = 0;              // turnos, pacientes o tratamientos
    private BigDecimal monto = BigDecimal.ZERO;   // solo para facturación
    private int porcentaje = 0;             // largo de la barrita (100 = el mayor)

    public FilaReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Suma una unidad y un importe a esta fila
    public void sumar(BigDecimal importe) {
        cantidad++;
        monto = monto.add(importe);
    }

    public String getEtiqueta() { return etiqueta; }
    public long getCantidad() { return cantidad; }
    public BigDecimal getMonto() { return monto; }
    public int getPorcentaje() { return porcentaje; }
    public void setPorcentaje(int porcentaje) { this.porcentaje = porcentaje; }
}