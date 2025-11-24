/*
 * Ubigeo.java
 * Representa un código de ubigeo y sus descriptores (departamento/provincia/distrito).
 * Utilizado para normalizar direcciones y mostrar localización en formularios.
 */
package model;

public class Ubigeo {
    // Código ubigeo (normalmente 6 dígitos) y sus componentes
    private String codigo;
    private String departamento;
    private String provincia;
    private String distrito;

    public Ubigeo(String codigo, String departamento, String provincia, String distrito) {
        this.codigo = codigo;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
    }
    /** Devuelve el código ubigeo (6 dígitos). */
    public String getCodigo() { return codigo; }

    /** Asigna el código ubigeo (6 dígitos). */
    public void setCodigo(String codigo) { this.codigo = codigo; }

    /** Devuelve el nombre del departamento. */
    public String getDepartamento() { return departamento; }

    /** Asigna el nombre del departamento. */
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    /** Devuelve el nombre de la provincia. */
    public String getProvincia() { return provincia; }

    /** Asigna el nombre de la provincia. */
    public void setProvincia(String provincia) { this.provincia = provincia; }

    /** Devuelve el nombre del distrito. */
    public String getDistrito() { return distrito; }

    /** Asigna el nombre del distrito. */
    public void setDistrito(String distrito) { this.distrito = distrito; }
}
