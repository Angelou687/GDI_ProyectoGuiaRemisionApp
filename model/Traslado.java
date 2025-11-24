/*
 * Traslado.java
 * Modelo que representa una operación de traslado real asociada a una guía.
 * Contiene información de vehículo, conductor, ventanas temporales (fecha
 * inicio/fin), estado y observaciones.
 */
package model;

import java.sql.Timestamp;

public class Traslado {

    // Identificador del traslado y relación con la guía
    private String codigoTraslado;
    private String codigoGuia;
    private String placa;
    private String licencia;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private String estadoTraslado;
    private String observaciones;

    public Traslado() {}
    /** Constructor vacío. */
    public Traslado() {}

    /**
     * Constructor completo de Traslado.
     */
    public Traslado(String codigoTraslado, String codigoGuia,
                    String placa, String licencia,
                    Timestamp fechaInicio, Timestamp fechaFin,
                    String estadoTraslado, String observaciones) {
        this.codigoTraslado = codigoTraslado;
        this.codigoGuia = codigoGuia;
        this.placa = placa;
        this.licencia = licencia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estadoTraslado = estadoTraslado;
        this.observaciones = observaciones;
    }

    /** Identificador interno del traslado. */
    public String getCodigoTraslado() { return codigoTraslado; }

    /** Asigna el identificador del traslado. */
    public void setCodigoTraslado(String codigoTraslado) { this.codigoTraslado = codigoTraslado; }

    /** Código de la guía asociada a este traslado. */
    public String getCodigoGuia() { return codigoGuia; }

    /** Asigna la guía asociada al traslado. */
    public void setCodigoGuia(String codigoGuia) { this.codigoGuia = codigoGuia; }

    /** Matrícula del vehículo que realiza el traslado. */
    public String getPlaca() { return placa; }

    /** Asigna la matrícula del vehículo. */
    public void setPlaca(String placa) { this.placa = placa; }

    /** Licencia del conductor responsable. */
    public String getLicencia() { return licencia; }

    /** Asigna la licencia del conductor. */
    public void setLicencia(String licencia) { this.licencia = licencia; }

    /** Fecha/hora de inicio programado/real del traslado. */
    public Timestamp getFechaInicio() { return fechaInicio; }

    /** Asigna la fecha/hora de inicio del traslado. */
    public void setFechaInicio(Timestamp fechaInicio) { this.fechaInicio = fechaInicio; }

    /** Fecha/hora de fin del traslado (si aplica). */
    public Timestamp getFechaFin() { return fechaFin; }

    /** Asigna la fecha/hora de fin del traslado. */
    public void setFechaFin(Timestamp fechaFin) { this.fechaFin = fechaFin; }

    /** Estado actual del traslado (ej. PENDIENTE, EN_CURSO, FINALIZADO). */
    public String getEstadoTraslado() { return estadoTraslado; }

    /** Asigna el estado actual del traslado. */
    public void setEstadoTraslado(String estadoTraslado) { this.estadoTraslado = estadoTraslado; }

    /** Observaciones libres asociadas al traslado. */
    public String getObservaciones() { return observaciones; }

    /** Asigna observaciones libres. */
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return codigoTraslado + " (" + codigoGuia + ")";
    }
}
