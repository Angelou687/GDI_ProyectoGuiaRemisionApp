/*
 * DetalleOrden.java
 * Representa una línea de una orden de pago: cantidad, precio unitario y subtotal.
 * Utilizado por las pantallas de gestión de órdenes y para reconstruir detalles
 * cuando se necesita compatibilidad con tablas antiguas.
 */
package model;

import java.math.BigDecimal;

public class DetalleOrden {
    // índice/orden dentro de la orden y valores monetarios como BigDecimal
    private int numeroItem;
    private String codigoOrden;
    private String codigoProducto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleOrden() {}
    /** Constructor vacío. */
    public DetalleOrden() {}

    /** Constructor que calcula subtotal automáticamente. */
    public DetalleOrden(String codigoOrden, String codigoProducto, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.codigoOrden = codigoOrden;
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad.multiply(precioUnitario);
    }

    /** Devuelve el número de ítem dentro de la orden. */
    public int getNumeroItem() { return numeroItem; }

    /** Asigna el número de ítem dentro de la orden. */
    public void setNumeroItem(int numeroItem) { this.numeroItem = numeroItem; }

    /** Código de la orden asociada. */
    public String getCodigoOrden() { return codigoOrden; }

    /** Asigna el código de la orden asociada. */
    public void setCodigoOrden(String codigoOrden) { this.codigoOrden = codigoOrden; }

    /** Código del producto en la línea. */
    public String getCodigoProducto() { return codigoProducto; }

    /** Asigna el código del producto en la línea. */
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    /** Devuelve la cantidad solicitada. */
    public BigDecimal getCantidad() { return cantidad; }

    /** Asigna la cantidad y se puede recalcular el subtotal externamente. */
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    /** Devuelve el precio unitario. */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    /** Asigna el precio unitario. */
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    /** Devuelve el subtotal (cantidad * precioUnitario). */
    public BigDecimal getSubtotal() { return subtotal; }

    /** Asigna el subtotal (usado en casos especiales). */
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
