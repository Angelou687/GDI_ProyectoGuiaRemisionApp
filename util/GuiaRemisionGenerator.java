package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Generador de PDF para Guía de Remisión (diseño parecido a la imagen provista).
 * Usa OpenPDF (com.github.librepdf:openpdf) / API compatible com.lowagie.text.
 *
 * Método principal: createPdf(...) que recibe placeholders para todos los datos dinámicos.
 *
 * Nota: ajustar paths/encoding/estilos según necesites.
 */
public class GuiaRemisionGenerator {

    public static class DetalleGuia {
        public int nro;
        public String bienNormalizado;
        public String codigoBien;
        public String codigoProductoSunat;
        public String partidaArancelaria;
        public String codigoGtin;
        public String descripcion;
        public String unidadMedida;
        public String cantidad;

        public DetalleGuia(int nro, String bienNormalizado, String codigoBien,
                           String codigoProductoSunat, String partidaArancelaria,
                           String codigoGtin, String descripcion,
                           String unidadMedida, String cantidad) {
            this.nro = nro;
            this.bienNormalizado = bienNormalizado;
            this.codigoBien = codigoBien;
            this.codigoProductoSunat = codigoProductoSunat;
            this.partidaArancelaria = partidaArancelaria;
            this.codigoGtin = codigoGtin;
            this.descripcion = descripcion;
            this.unidadMedida = unidadMedida;
            this.cantidad = cantidad;
        }
    }

    /**
     * Crea el PDF de Guía de Remisión.
     *
     * @param destPath           Ruta destino del PDF
     * @param empresaNombre      Nombre de la empresa emisora
     * @param rucEmisor          RUC del emisor
     * @param rucRemitente       RUC del remitente (puede ser igual al emisor)
     * @param guiaSerie          Serie (ej. EG07)
     * @param guiaCorrelativo    Correlativo (ej. 00000395)
     * @param fechaEmision       Fecha y hora de emisión (texto)
     * @param fechaInicioTraslado Fecha inicio traslado (texto)
     * @param puntoPartida       Texto punto de partida (puede contener saltos)
     * @param puntoLlegada       Texto punto de llegada (puede contener saltos)
     * @param motivo             Motivo de traslado
     * @param destinatarioNombre Nombre / razón social destinatario
     * @param destinatarioRuc    RUC destinatario
     * @param items              Lista de ítems (DetalleGuia)
     * @param unidadPeso         Unidad de medida de peso (ej. KGM)
     * @param pesoBrutoTotal     Peso bruto total (cadena formateada)
     * @param modalidad          Modalidad de traslado (Privado/Público)
     * @param indicadorTransbordo Indicador transbordo (SI/NO)
     * @param indicadorRetornoEnvases Indicador retorno envases (SI/NO)
     * @param placa              Placa principal
     * @param autorizacionVehiculo Entidad / número autorización especial (texto)
     * @param nombreConductor    Nombre completo conductor
     * @param licenciaConductor  Número de licencia
     * @param qrImageBytes       Opcional bytes imagen QR (null si no hay)
     * @throws Exception si falla creación
     */
    public static void createPdf(String destPath,
                                 String empresaNombre,
                                 String rucEmisor,
                                 String rucRemitente,
                                 String guiaSerie,
                                 String guiaCorrelativo,
                                 String fechaEmision,
                                 String fechaInicioTraslado,
                                 String puntoPartida,
                                 String puntoLlegada,
                                 String motivo,
                                 String destinatarioNombre,
                                 String destinatarioRuc,
                                 List<DetalleGuia> items,
                                 String unidadPeso,
                                 String pesoBrutoTotal,
                                 String modalidad,
                                 String indicadorTransbordo,
                                 String indicadorRetornoEnvases,
                                 String placa,
                                 String autorizacionVehiculo,
                                 String nombreConductor,
                                 String licenciaConductor,
                                 byte[] qrImageBytes) throws Exception {

        // Documento A4 horizontal (landscape) según instrucción
        Document doc = new Document(PageSize.A4.rotate(), 30f, 30f, 30f, 30f);
        PdfWriter.getInstance(doc, new FileOutputStream(destPath));
        doc.open();

        // Fuentes
        Font fontBase = FontFactory.getFont(FontFactory.HELVETICA, 8f, Font.NORMAL, java.awt.Color.BLACK);
        Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA, 7f, Font.BOLD, java.awt.Color.BLACK);
        Font fontRuc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Font.BOLD, java.awt.Color.BLACK);
        Font fontCompany = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f, Font.BOLD, java.awt.Color.BLACK);
        Font fontSmallItalic = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7f, Font.NORMAL, java.awt.Color.DARK_GRAY);

        // ======================
        // CABECERA (3 columnas)
        // ======================
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100f);
        header.setWidths(new float[]{15f, 50f, 35f});
        header.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Columna 1: QR
        if (qrImageBytes != null) {
            Image qr = Image.getInstance(qrImageBytes);
            qr.scaleToFit(90f, 90f);
            PdfPCell imgCell = new PdfPCell(qr, false);
            imgCell.setBorder(Rectangle.NO_BORDER);
            imgCell.setVerticalAlignment(Element.ALIGN_TOP);
            imgCell.setPadding(2f);
            header.addCell(imgCell);
        } else {
            PdfPCell placeholder = new PdfPCell(new Phrase("QR", fontBase));
            placeholder.setFixedHeight(60f);
            placeholder.setBorder(Rectangle.NO_BORDER);
            placeholder.setVerticalAlignment(Element.ALIGN_TOP);
            placeholder.setHorizontalAlignment(Element.ALIGN_LEFT);
            placeholder.setPadding(2f);
            header.addCell(placeholder);
        }

        // Columna 2: Nombre empresa y fecha
        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(2f);

        Paragraph pEmpresa = new Paragraph(empresaNombre, fontCompany);
        pEmpresa.setAlignment(Element.ALIGN_LEFT);
        c2.addElement(pEmpresa);

        Paragraph pFecha = new Paragraph("Fecha y hora de emisión : " + (fechaEmision == null ? "" : fechaEmision), fontSmallItalic);
        pFecha.setSpacingBefore(4f);
        c2.addElement(pFecha);

        header.addCell(c2);

        // Columna 3: recuadro RUC con borde visible (tabla anidada)
        PdfPTable cajaRuc = new PdfPTable(1);
        cajaRuc.setWidthPercentage(100f);
        PdfPCell rucCell1 = new PdfPCell(new Phrase("RUC " + (rucEmisor == null ? "" : rucEmisor), fontRuc));
        rucCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        rucCell1.setBorder(Rectangle.NO_BORDER);
        rucCell1.setPadding(4f);

        PdfPCell titleCell = new PdfPCell(new Phrase("GUÍA DE REMISIÓN ELECTRÓNICA", fontTableHeader));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(2f);

        PdfPCell remitenteCell = new PdfPCell(new Phrase("REMITENTE", fontTableHeader));
        remitenteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        remitenteCell.setBorder(Rectangle.NO_BORDER);
        remitenteCell.setPadding(2f);

        PdfPCell numeroCell = new PdfPCell(new Phrase("N° " + guiaSerie + " - " + guiaCorrelativo, fontTableHeader));
        numeroCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numeroCell.setBorder(Rectangle.NO_BORDER);
        numeroCell.setPadding(4f);

        cajaRuc.addCell(rucCell1);
        cajaRuc.addCell(titleCell);
        cajaRuc.addCell(remitenteCell);
        cajaRuc.addCell(numeroCell);

        PdfPCell cajaWrap = new PdfPCell(cajaRuc);
        cajaWrap.setPadding(6f);
        cajaWrap.setBorder(Rectangle.BOX); // borde visible del recuadro RUC
        cajaWrap.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(cajaWrap);

        doc.add(header);

        // Espacio
        doc.add(Chunk.NEWLINE);

        // ======================
        // INFO TRASLADO (2 columnas)
        // ======================
        PdfPTable infoTraslado = new PdfPTable(2);
        infoTraslado.setWidthPercentage(100f);
        infoTraslado.setWidths(new float[]{40f, 60f});
        infoTraslado.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        infoTraslado.getDefaultCell().setPadding(2f);

        PdfPCell itLeft = new PdfPCell();
        itLeft.setBorder(Rectangle.NO_BORDER);
        itLeft.addElement(new Paragraph("Fecha de inicio de Traslado : " + (fechaInicioTraslado == null ? "" : fechaInicioTraslado), fontBase));
        itLeft.addElement(new Paragraph("Motivo de Traslado : " + (motivo == null ? "" : motivo), fontBase));
        infoTraslado.addCell(itLeft);

        PdfPCell itRight = new PdfPCell();
        itRight.setBorder(Rectangle.NO_BORDER);
        Paragraph pPartida = new Paragraph("Punto de Partida:", fontBase);
        pPartida.add(new Chunk(" " + (puntoPartida == null ? "" : puntoPartida), fontBase));
        Paragraph pLlegada = new Paragraph("Punto de llegada:", fontBase);
        pLlegada.add(new Chunk(" " + (puntoLlegada == null ? "" : puntoLlegada), fontBase));
        itRight.addElement(pPartida);
        itRight.addElement(pLlegada);
        infoTraslado.addCell(itRight);

        doc.add(infoTraslado);

        // Espacio
        doc.add(Chunk.NEWLINE);

        // ======================
        // DESTINATARIO
        // ======================
        PdfPTable tablaDest = new PdfPTable(1);
        tablaDest.setWidthPercentage(100f);
        tablaDest.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        PdfPCell destCell = new PdfPCell(new Phrase("Datos del Destinatario : " + (destinatarioNombre == null ? "" : destinatarioNombre)
                + " - RUC: " + (destinatarioRuc == null ? "" : destinatarioRuc), fontBase));
        destCell.setBorder(Rectangle.NO_BORDER);
        destCell.setPadding(4f);
        tablaDest.addCell(destCell);
        doc.add(tablaDest);

        doc.add(Chunk.NEWLINE);

        // ======================
        // TABLA DE ITEMS (9 columnas)
        // ======================
        PdfPTable tableItems = new PdfPTable(9);
        tableItems.setWidthPercentage(100f);
        // anchuras relativas: hacer la columna descripción más ancha
        tableItems.setWidths(new float[]{25f, 50f, 55f, 60f, 55f, 45f, 200f, 45f, 45f});

        // Header
        String[] headers = {"N°", "Bien normalizado", "Código de Bien", "Código producto SUNAT", "Partida arancelaria",
                "Código GTIN", "Descripción Detallada", "Unidad de medida", "Cantidad"};
        for (String h : headers) {
            PdfPCell hc = new PdfPCell(new Phrase(h, fontTableHeader));
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            // fondo gris muy claro
            hc.setBackgroundColor(new java.awt.Color(0xF2, 0xF2, 0xF2));
            hc.setPadding(4f);
            hc.setBorder(Rectangle.BOX); // bordes visibles en la tabla de productos
            tableItems.addCell(hc);
        }

        // Body
        if (items != null) {
            for (DetalleGuia it : items) {
                PdfPCell cN = new PdfPCell(new Phrase(String.valueOf(it.nro), fontBase));
                cN.setPadding(4f); cN.setBorder(Rectangle.BOX); cN.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableItems.addCell(cN);

                PdfPCell cBN = new PdfPCell(new Phrase(it.bienNormalizado == null ? "" : it.bienNormalizado, fontBase));
                cBN.setPadding(4f); cBN.setBorder(Rectangle.BOX);
                tableItems.addCell(cBN);

                PdfPCell cCB = new PdfPCell(new Phrase(it.codigoBien == null ? "" : it.codigoBien, fontBase));
                cCB.setPadding(4f); cCB.setBorder(Rectangle.BOX);
                tableItems.addCell(cCB);

                PdfPCell cCP = new PdfPCell(new Phrase(it.codigoProductoSunat == null ? "" : it.codigoProductoSunat, fontBase));
                cCP.setPadding(4f); cCP.setBorder(Rectangle.BOX);
                tableItems.addCell(cCP);

                PdfPCell cPA = new PdfPCell(new Phrase(it.partidaArancelaria == null ? "" : it.partidaArancelaria, fontBase));
                cPA.setPadding(4f); cPA.setBorder(Rectangle.BOX);
                tableItems.addCell(cPA);

                PdfPCell cGTIN = new PdfPCell(new Phrase(it.codigoGtin == null ? "" : it.codigoGtin, fontBase));
                cGTIN.setPadding(4f); cGTIN.setBorder(Rectangle.BOX);
                tableItems.addCell(cGTIN);

                PdfPCell cDesc = new PdfPCell(new Phrase(it.descripcion == null ? "" : it.descripcion, fontBase));
                cDesc.setPadding(4f); cDesc.setBorder(Rectangle.BOX);
                tableItems.addCell(cDesc);

                PdfPCell cUM = new PdfPCell(new Phrase(it.unidadMedida == null ? "" : it.unidadMedida, fontBase));
                cUM.setPadding(4f); cUM.setBorder(Rectangle.BOX); cUM.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableItems.addCell(cUM);

                PdfPCell cQty = new PdfPCell(new Phrase(it.cantidad == null ? "" : it.cantidad, fontBase));
                cQty.setPadding(4f); cQty.setBorder(Rectangle.BOX); cQty.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableItems.addCell(cQty);
            }
        } else {
            // fila vacía
            PdfPCell empty = new PdfPCell(new Phrase("Sin items", fontBase));
            empty.setColspan(9);
            empty.setPadding(8f);
            empty.setBorder(Rectangle.BOX);
            tableItems.addCell(empty);
        }

        doc.add(tableItems);

        doc.add(Chunk.NEWLINE);

        // ======================
        // PIE: Unidad de medida y peso total
        // ======================
        PdfPTable footer1 = new PdfPTable(2);
        footer1.setWidthPercentage(100f);
        footer1.setWidths(new float[]{50f, 50f});
        footer1.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell uMed = new PdfPCell(new Phrase("Unidad de Medida del Peso Bruto: " + (unidadPeso == null ? "" : unidadPeso), fontBase));
        uMed.setBorder(Rectangle.NO_BORDER);
        uMed.setPadding(3f);
        footer1.addCell(uMed);

        PdfPCell pesoCell = new PdfPCell(new Phrase("Peso Bruto total de la carga: " + (pesoBrutoTotal == null ? "" : pesoBrutoTotal), fontBase));
        pesoCell.setBorder(Rectangle.NO_BORDER);
        pesoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        pesoCell.setPadding(3f);
        footer1.addCell(pesoCell);

        doc.add(footer1);

        doc.add(Chunk.NEWLINE);

        // ======================
        // Datos del traslado (2 columnas sin bordes)
        // ======================
        PdfPTable trasladoData = new PdfPTable(2);
        trasladoData.setWidthPercentage(100f);
        trasladoData.setWidths(new float[]{50f, 50f});
        trasladoData.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell tLeft = new PdfPCell();
        tLeft.setBorder(Rectangle.NO_BORDER);
        tLeft.addElement(new Phrase("Datos del traslado:", fontTableHeader));
        tLeft.addElement(new Phrase("Modalidad de Traslado: " + (modalidad == null ? "" : modalidad), fontBase));
        tLeft.addElement(new Phrase("Indicador de transbordo programado: " + (indicadorTransbordo == null ? "" : indicadorTransbordo), fontBase));
        tLeft.addElement(new Phrase("Indicador de retorno de vehículo con envases o embalajes vacíos: " + (indicadorRetornoEnvases == null ? "" : indicadorRetornoEnvases), fontBase));
        trasladoData.addCell(tLeft);

        PdfPCell tRight = new PdfPCell();
        tRight.setBorder(Rectangle.NO_BORDER);
        tRight.addElement(new Phrase(" ", fontBase)); // espacio para equilibrio visual
        trasladoData.addCell(tRight);

        doc.add(trasladoData);

        doc.add(Chunk.NEWLINE);

        // ======================
        // Datos de vehículos y conductores
        // ======================
        PdfPTable tablaVehCon = new PdfPTable(2);
        tablaVehCon.setWidthPercentage(100f);
        tablaVehCon.setWidths(new float[]{50f, 50f});
        tablaVehCon.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell vehCell = new PdfPCell();
        vehCell.setBorder(Rectangle.NO_BORDER);
        vehCell.addElement(new Phrase("Datos de los vehículos:", fontTableHeader));
        vehCell.addElement(new Phrase("Principal - Número de placa: " + (placa == null ? "" : placa), fontBase));
        if (autorizacionVehiculo != null && !autorizacionVehiculo.isEmpty()) {
            vehCell.addElement(new Phrase("Entidad emisora de autorización especial: " + autorizacionVehiculo, fontBase));
        }
        tablaVehCon.addCell(vehCell);

        PdfPCell condCell = new PdfPCell();
        condCell.setBorder(Rectangle.NO_BORDER);
        condCell.addElement(new Phrase("Datos de los conductores:", fontTableHeader));
        condCell.addElement(new Phrase("Principal: " + (nombreConductor == null ? "" : nombreConductor), fontBase));
        condCell.addElement(new Phrase("Número de licencia de conducir: " + (licenciaConductor == null ? "" : licenciaConductor), fontBase));
        tablaVehCon.addCell(condCell);

        doc.add(tablaVehCon);

        // Cerrar doc
        doc.close();
    }
}