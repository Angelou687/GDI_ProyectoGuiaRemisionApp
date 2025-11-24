/*
 * GeneratePdfDemo.java
 * Pequeño demo/ejemplo que muestra cómo usar `GuiaRemisionGenerator` para
 * crear un PDF de guía de remisión con datos de ejemplo. Este archivo sirve
 * como referencia para integrar la generación de PDFs en la aplicación.
 */
package app;

import util.GuiaRemisionGenerator;
import util.GuiaRemisionGenerator.DetalleGuia;

import java.util.ArrayList;
import java.util.List;

public class GeneratePdfDemo {
    public static void main(String[] args) {
        try {
            String out = "C:\\Users\\Admin\\Documents\\GuiaRemision_ejemplo.pdf";

            // Datos de ejemplo (reemplaza con tus datos reales)
            String empresaNombre = "RED LIPA ECOLOGICA SOCIEDAD ANONIMA CERRADA";
            String rucEmisor = "20604635943";
            String rucRemitente = rucEmisor;
            String serie = "EG07";
            String correlativo = "00000395";
            String fechaEmision = "29/08/2025 08:01 AM";
            String fechaInicio = "29/08/2025";
            String puntoPartida = "MZ. N LOTE, 11 OTR. CENTRO INDUSTRIAL LAS CANTERAS - CERRO COLORADO - AREQUIPA";
            String puntoLlegada = "JR. LA LIBERTAD MZ. 2 LOTE 3 - CERRO COLORADO - AREQUIPA";
            String motivo = "Venta sujeta a confirmación del comprador";
            String destinatario = "EMPRESA CONSERVADORA AMBIENTAL DEL SUR E.I.R.L.";
            String destinatarioRuc = "20455283915";

            List<DetalleGuia> items = new ArrayList<>();
            items.add(new DetalleGuia(1, "NO", "C001", "0000001", "010121", "0000000000017",
                    "CARTON CORRUGADO EN DESUSO", "KILOGRAMO", "1310.00"));

            String unidadPeso = "KGM";
            String pesoTotal = "1,310.00";
            String modalidad = "Privado";
            String indicadorTransbordo = "NO";
            String indicadorRetornoEnvases = "NO";
            String placa = "VBR833";
            String autorizacionVehiculo = "MTC - 15M22047831E";
            String nombreConductor = "CONDORI LIPA GERSON EVERTH";
            String licencia = "H48878047";

            // Si tienes bytes del QR, pásalos; si no, deja null
            byte[] qrBytes = null;

            GuiaRemisionGenerator.createPdf(
                    out,
                    empresaNombre,
                    rucEmisor,
                    rucRemitente,
                    serie,
                    correlativo,
                    fechaEmision,
                    fechaInicio,
                    puntoPartida,
                    puntoLlegada,
                    motivo,
                    destinatario,
                    destinatarioRuc,
                    items,
                    unidadPeso,
                    pesoTotal,
                    modalidad,
                    indicadorTransbordo,
                    indicadorRetornoEnvases,
                    placa,
                    autorizacionVehiculo,
                    nombreConductor,
                    licencia,
                    qrBytes
            );

            System.out.println("PDF generado en: " + out);
        } catch (Exception ex) {
            System.err.println("Error creando PDF: " + ex.getMessage());
        }
    }
}