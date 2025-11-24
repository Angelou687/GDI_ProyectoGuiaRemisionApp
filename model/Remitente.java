/*
 * Remitente.java
 * Modelo para la entidad remitente (emisor de guías). Incluye datos fiscales
 * y de contacto necesarios para emitir una guía.
 */
package model;

public class Remitente {
    // RUC y datos de la empresa remitente
    private String ruc;
    private String nombreEmpresa;
    private String razonSocial;
    private String telefono;
    private String email;
    private String calleDireccion;
    private String codigoUbigeo;

    public Remitente() {}
    /** Constructor vacío. */
    public Remitente() {}

    /** Constructor completo del remitente/emisor. */
    public Remitente(String ruc, String nombreEmpresa, String razonSocial,
                     String telefono, String email, String calleDireccion, String codigoUbigeo) {
        this.ruc = ruc;
        this.nombreEmpresa = nombreEmpresa;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.email = email;
        this.calleDireccion = calleDireccion;
        this.codigoUbigeo = codigoUbigeo;
    }

    /** Devuelve el RUC de la empresa remitente. */
    public String getRuc() { return ruc; }

    /** Asigna el RUC de la empresa remitente. */
    public void setRuc(String ruc) { this.ruc = ruc; }

    /** Devuelve el nombre comercial de la empresa. */
    public String getNombreEmpresa() { return nombreEmpresa; }

    /** Asigna el nombre comercial de la empresa. */
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    /** Devuelve la razón social completa. */
    public String getRazonSocial() { return razonSocial; }

    /** Asigna la razón social. */
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    /** Devuelve el teléfono de la empresa. */
    public String getTelefono() { return telefono; }

    /** Asigna el teléfono de la empresa. */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** Devuelve el correo electrónico de contacto. */
    public String getEmail() { return email; }

    /** Asigna el correo electrónico de contacto. */
    public void setEmail(String email) { this.email = email; }

    /** Devuelve la dirección física (calle). */
    public String getCalleDireccion() { return calleDireccion; }

    /** Asigna la dirección física (calle). */
    public void setCalleDireccion(String calleDireccion) { this.calleDireccion = calleDireccion; }

    /** Devuelve el código ubigeo asociado. */
    public String getCodigoUbigeo() { return codigoUbigeo; }

    /** Asigna el código ubigeo asociado. */
    public void setCodigoUbigeo(String codigoUbigeo) { this.codigoUbigeo = codigoUbigeo; }
}
