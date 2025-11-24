/*
 * OrdenDePago.java
 * Modelo ligero para representar una orden de pago: código, fecha, RUC del
 * cliente y estado. Usado por el módulo de órdenes y por los DAOs.
 */
package model;

import java.sql.Date;

public class OrdenDePago {
    // Identificador y metadatos de la orden
    private String codigoOrden;
    private Date fecha;
    private String rucCliente;
    private String estado;

    public OrdenDePago() {}
    /** Constructor vacío. */
    public OrdenDePago() {}

    /** Constructor completo de la orden de pago. */
    public OrdenDePago(String codigoOrden, Date fecha, String rucCliente, String estado) {
        this.codigoOrden = codigoOrden;
        this.fecha = fecha;
        this.rucCliente = rucCliente;
        this.estado = estado;
    }

    /** Devuelve el código de la orden. */
    public String getCodigoOrden() { return codigoOrden; }

    /** Asigna el código de la orden. */
    public void setCodigoOrden(String codigoOrden) { this.codigoOrden = codigoOrden; }

    /** Devuelve la fecha de la orden. */
    public Date getFecha() { return fecha; }

    /** Asigna la fecha de la orden. */
    public void setFecha(Date fecha) { this.fecha = fecha; }

    /** Devuelve el RUC del cliente asociado a la orden. */
    public String getRucCliente() { return rucCliente; }

    /** Asigna el RUC del cliente asociado a la orden. */
    public void setRucCliente(String rucCliente) { this.rucCliente = rucCliente; }

    /** Devuelve el estado de la orden (ej. PENDIENTE, PAGADO). */
    public String getEstado() { return estado; }

    /** Asigna el estado de la orden. */
    public void setEstado(String estado) { this.estado = estado; }
}
