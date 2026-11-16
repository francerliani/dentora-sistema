package com.dentora.dentora.service;

import com.dentora.dentora.model.Consulta;
import com.dentora.dentora.model.DetalleTratamiento;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.repository.ConsultaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// Arma el PDF de la historia clínica de un paciente (devolución de los profesores)
@Service
public class HistoriaClinicaPdfService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ConsultaRepository consultaRepository;

    public HistoriaClinicaPdfService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    // Devuelve el PDF como bytes (el controlador los manda al navegador para descargar)
    @Transactional(readOnly = true)
    public byte[] generar(Paciente paciente) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();   // el PDF se escribe en memoria

        // Hoja A4 con márgenes de 40 puntos
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, salida);
        doc.open();

        // Tipos de letra
        Font titulo  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(18, 41, 91));
        Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normal  = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font chica   = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        // ===== Encabezado =====
        doc.add(new Paragraph("Dentora - Historia clínica", titulo));
        doc.add(new Paragraph("Emitida el " + LocalDate.now().format(FORMATO), chica));
        doc.add(new Paragraph(" "));   // renglón en blanco

        // ===== Datos del paciente =====
        String edad = (paciente.getFechaNacimiento() != null)
                ? Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears() + " años"
                : "sin cargar";
        String obraSocial = (paciente.getObraSocial() != null) ? paciente.getObraSocial().getNombre() : "Particular";

        doc.add(new Paragraph(paciente.getApellido() + ", " + paciente.getNombre(), negrita));
        doc.add(new Paragraph("DNI " + paciente.getDni() + "  ·  Edad: " + edad
                + "  ·  Obra social: " + obraSocial, normal));
        doc.add(new Paragraph(" "));

                // ===== Tabla del historial =====
        List<Consulta> consultas = consultaRepository.findByPacienteOrderByFechaDescIdConsultaDesc(paciente);

        if (consultas.isEmpty()) {
            doc.add(new Paragraph("El paciente no tiene consultas registradas.", normal));
        } else {
            // 4 columnas; los números son el ancho relativo de cada una
            PdfPTable tabla = new PdfPTable(new float[]{1.3f, 2.5f, 2f, 5.5f});
            tabla.setWidthPercentage(100);

            // Encabezados con fondo celeste
            for (String encabezado : List.of("Fecha", "Motivo", "Odontólogo", "Tratamientos")) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, negrita));
                celda.setBackgroundColor(new Color(227, 234, 245));
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            // Una fila por consulta
            for (Consulta c : consultas) {
                tabla.addCell(celda(c.getFecha().format(FORMATO), normal));
                tabla.addCell(celda(c.getMotivo(), normal));
                tabla.addCell(celda(c.getOdontologo().getNombre(), normal));
                tabla.addCell(celda(textoTratamientos(c), normal));
            }
            doc.add(tabla);
        }

        doc.close();
        return salida.toByteArray();
    }

    // Arma el texto de la columna Tratamientos: un bloque por cada detalle
    private String textoTratamientos(Consulta c) {
        if (c.getDetalles().isEmpty()) {
            return "Sin tratamientos";   // RN-17
        }
        StringBuilder texto = new StringBuilder();
        for (DetalleTratamiento d : c.getDetalles()) {
            String caras = d.getCaras().stream().map(cara -> cara.getNombre()).collect(Collectors.joining(", "));
            texto.append(d.getDiente().getNumeroFDI()).append(" (").append(caras).append("): ")
                 .append(d.getTipoTratamiento().getNombre()).append(" - ").append(d.getEstado().getNombre())
                 .append("\n   Diagnóstico: ").append(d.getDiagnostico() != null ? d.getDiagnostico() : "-");
            if (d.getObservaciones() != null) {
                texto.append("\n   Observaciones: ").append(d.getObservaciones());
            }
            texto.append("\n");
        }
        return texto.toString().trim();
    }

    // Celda común con un poco de margen interno
    private PdfPCell celda(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(5);
        return celda;
    }
}