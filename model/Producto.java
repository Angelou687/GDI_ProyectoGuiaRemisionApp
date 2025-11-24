/*
 * Producto.java
 * POJO que contiene la información mínima de un producto: código, nombre,
 * precio base y unidad de medida. Usado por DAOs y formularios de producto.
 */
package model;

public class Producto {
    // Identificador de producto y atributos comerciales
    private String codigoProducto;
    private String nombreProducto;
    private double precioBase;
    private String unidadMedida;

    public Producto() {}
    /** Constructor por defecto. */
    public Producto() {}

    /** Constructor completo del producto. */
    public Producto(String codigoProducto, String nombreProducto, double precioBase, String unidadMedida) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.precioBase = precioBase;
        this.unidadMedida = unidadMedida;
    }

    /** Devuelve el código de producto. */
    public String getCodigoProducto() { return codigoProducto; }

    /** Asigna el código de producto. */
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    /** Devuelve el nombre del producto. */
    public String getNombreProducto() { return nombreProducto; }

    /** Asigna el nombre del producto. */
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    /** Devuelve el precio base (sin impuestos). */
    public double getPrecioBase() { return precioBase; }

    /** Asigna el precio base (sin impuestos). */
    public void setPrecioBase(double precioBase) { this.precioBase = precioBase; }

    /** Devuelve la unidad de medida (ej. KG, UND). */
    public String getUnidadMedida() { return unidadMedida; }

    /** Asigna la unidad de medida. */
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
}
