/*
 * Destinatario.java
 * POJO que representa un destinatario (cliente) de la guía.
 * Incluye RUC, nombre, teléfono, dirección y código de ubigeo.
 */
package model;

public class Destinatario {

    // Campos principales del destinatario
    private String ruc;
    private String nombre;
    private String numeroTelefono;
    private String calleDireccion;
    private String codigoUbigeo;
    private String gmail;

    public Destinatario() {}
    /**
     * Constructor completo del destinatario.
     */
    public Destinatario(String ruc, String nombre, String numeroTelefono,
                        String calleDireccion, String codigoUbigeo, String gmail) {
        this.ruc = ruc;
        this.nombre = nombre;
        this.numeroTelefono = numeroTelefono;
        this.calleDireccion = calleDireccion;
        this.codigoUbigeo = codigoUbigeo;
        this.gmail = gmail;
    }

    /** Devuelve el RUC del destinatario. */
    public String getRuc() { return ruc; }

    /** Asigna el RUC del destinatario. */
    public void setRuc(String ruc) { this.ruc = ruc; }

    /** Devuelve el nombre o razón social. */
    public String getNombre() { return nombre; }

    /** Asigna el nombre o razón social. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Devuelve el número de teléfono principal. */
    public String getNumeroTelefono() { return numeroTelefono; }

    /** Asigna el número de teléfono principal. */
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    /** Devuelve la dirección (calle y referencia). */
    public String getCalleDireccion() { return calleDireccion; }

    /** Asigna la dirección (calle y referencia). */
    public void setCalleDireccion(String calleDireccion) { this.calleDireccion = calleDireccion; }

    /** Devuelve el código ubigeo asociado (6 dígitos). */
    public String getCodigoUbigeo() { return codigoUbigeo; }

    /** Asigna el código ubigeo asociado (6 dígitos). */
    public void setCodigoUbigeo(String codigoUbigeo) { this.codigoUbigeo = codigoUbigeo; }

    /** Devuelve el email (campo opcional, aquí llamado gmail). */
    public String getGmail() { return gmail; }

    /** Asigna el email del destinatario. */
    public void setGmail(String gmail) { this.gmail = gmail; }
}
