/*
 * Conductor.java
 * Modelo que representa un conductor.
 * Campos principales: licencia, dni, nombre, teléfono y fecha de vencimiento
 * de la licencia. Es un POJO simple usado por la UI y los DAOs.
 */
package model;

import java.sql.Date;

public class Conductor {
    private String licencia;
    private String dni;
    private String nombre;
    private String telefono;
    private Date fechaVencimiento;

    public Conductor() {}
    /**
     * Constructor con valores completos.
     * @param licencia Número de licencia del conductor
     * @param dni Documento de identidad
     * @param nombre Nombre completo
     * @param telefono Teléfono de contacto
     * @param fechaVencimiento Fecha de vencimiento de la licencia
     */
    public Conductor(String licencia, String dni, String nombre, String telefono, Date fechaVencimiento) {
        this.licencia = licencia;
        this.dni = dni;
        this.nombre = nombre;
        this.telefono = telefono;
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * Devuelve la licencia del conductor.
     */
    public String getLicencia() { return licencia; }

    /**
     * Establece la licencia del conductor.
     */
    public void setLicencia(String licencia) { this.licencia = licencia; }

    /**
     * Devuelve el DNI del conductor.
     */
    public String getDni() { return dni; }

    /**
     * Establece el DNI del conductor.
     */
    public void setDni(String dni) { this.dni = dni; }

    /**
     * Devuelve el nombre completo.
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre completo.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Devuelve el teléfono de contacto.
     */
    public String getTelefono() { return telefono; }

    /**
     * Establece el teléfono de contacto.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Devuelve la fecha de vencimiento de la licencia.
     */
    public Date getFechaVencimiento() { return fechaVencimiento; }

    /**
     * Establece la fecha de vencimiento de la licencia.
     */
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
