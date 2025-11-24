/*
 * Vehiculo.java
 * POJO para información de vehículos: placa, número MTC, tipo, marca, modelo
 * y carga máxima. Usado por formularios y por el módulo de traslados.
 */
package model;

public class Vehiculo {
    // Datos identificadores y características del vehículo
    private String placa;
    private String numeroMtc;
    private String tipoVehiculo;
    private String marca;
    private String modelo;
    private double cargaMax;

    public Vehiculo() {}
    /** Constructor por defecto. */
    public Vehiculo() {}

    /**
     * Constructor completo del vehículo.
     * @param placa Matrícula del vehículo
     * @param numeroMtc Número MTC/registro
     * @param tipoVehiculo Tipo (ej. camión, furgon)
     * @param marca Marca comercial
     * @param modelo Modelo
     * @param cargaMax Capacidad máxima de carga (kg)
     */
    public Vehiculo(String placa, String numeroMtc, String tipoVehiculo, String marca, String modelo, double cargaMax) {
        this.placa = placa;
        this.numeroMtc = numeroMtc;
        this.tipoVehiculo = tipoVehiculo;
        this.marca = marca;
        this.modelo = modelo;
        this.cargaMax = cargaMax;
    }

    /** Devuelve la matrícula/placa. */
    public String getPlaca() { return placa; }

    /** Asigna la matrícula/placa. */
    public void setPlaca(String placa) { this.placa = placa; }

    /** Devuelve el número MTC o registro. */
    public String getNumeroMtc() { return numeroMtc; }

    /** Asigna el número MTC o registro. */
    public void setNumeroMtc(String numeroMtc) { this.numeroMtc = numeroMtc; }

    /** Devuelve el tipo de vehículo. */
    public String getTipoVehiculo() { return tipoVehiculo; }

    /** Asigna el tipo de vehículo. */
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    /** Devuelve la marca del vehículo. */
    public String getMarca() { return marca; }

    /** Asigna la marca del vehículo. */
    public void setMarca(String marca) { this.marca = marca; }

    /** Devuelve el modelo del vehículo. */
    public String getModelo() { return modelo; }

    /** Asigna el modelo del vehículo. */
    public void setModelo(String modelo) { this.modelo = modelo; }

    /** Devuelve la carga máxima en kg. */
    public double getCargaMax() { return cargaMax; }

    /** Asigna la carga máxima en kg. */
    public void setCargaMax(double cargaMax) { this.cargaMax = cargaMax; }
}
