/*
 * DetalleGuia.java
 * Modelo que representa una línea del detalle de una guía de remisión.
 * Contiene campos para normalización del bien, códigos (producto/SUNAT/GTIN),
 * unidad de medida y cantidad. Usado por `GuiaRemisionGenerator` y DAOs.
 */
package model;

public class DetalleGuia {
    // Número de línea y campos descriptivos del ítem
    private int nro;
    private String bienNormalizado;
    private String codigoBien;
    private String codigoProductoSunat;
    private String partidaArancelaria;
    private String codigoGtin;
    private String descripcion;
    private String unidadMedida;
    private String cantidad;

    public DetalleGuia() {}
    /** Constructor por defecto. */
    public DetalleGuia() {}

    /** Constructor completo de la línea de detalle de guía. */
    public DetalleGuia(int nro, String bienNormalizado, String codigoBien, String codigoProductoSunat,
                       String partidaArancelaria, String codigoGtin, String descripcion,
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

    // getters / setters con documentación breve
    /** Número de línea dentro del detalle. */
    public int getNro() { return nro; }

    /** Asigna el número de línea dentro del detalle. */
    public void setNro(int nro) { this.nro = nro; }

    /** Nombre normalizado del bien. */
    public String getBienNormalizado() { return bienNormalizado; }

    /** Asigna el nombre normalizado del bien. */
    public void setBienNormalizado(String bienNormalizado) { this.bienNormalizado = bienNormalizado; }

    /** Código interno del bien. */
    public String getCodigoBien() { return codigoBien; }

    /** Asigna el código interno del bien. */
    public void setCodigoBien(String codigoBien) { this.codigoBien = codigoBien; }

    /** Código SUNAT del producto si aplica. */
    public String getCodigoProductoSunat() { return codigoProductoSunat; }

    /** Asigna el código SUNAT del producto. */
    public void setCodigoProductoSunat(String codigoProductoSunat) { this.codigoProductoSunat = codigoProductoSunat; }

    /** Partida arancelaria asociada (opcional). */
    public String getPartidaArancelaria() { return partidaArancelaria; }

    /** Asigna la partida arancelaria. */
    public void setPartidaArancelaria(String partidaArancelaria) { this.partidaArancelaria = partidaArancelaria; }

    /** Código GTIN/EAN del producto (opcional). */
    public String getCodigoGtin() { return codigoGtin; }

    /** Asigna el código GTIN/EAN. */
    public void setCodigoGtin(String codigoGtin) { this.codigoGtin = codigoGtin; }

    /** Descripción libre del ítem. */
    public String getDescripcion() { return descripcion; }

    /** Asigna la descripción del ítem. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Unidad de medida del ítem. */
    public String getUnidadMedida() { return unidadMedida; }

    /** Asigna la unidad de medida del ítem. */
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    /** Cantidad indicada en formato texto (se sigue el formato original del proyecto). */
    public String getCantidad() { return cantidad; }

    /** Asigna la cantidad como texto. */
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }
}