package model;

import java.sql.Date;
import java.sql.Time;

/**
 * CabeceraGuia.java
 * Modelo que representa la cabecera de una Guía de Remisión.
 * Contiene campos principales usados por la UI y por la persistencia:
 * - identificador `codigoGuia`, serie/numero, referencias a orden, remitente
 *   y destinatario, direcciones, ubigeos, peso y detalle temporal (fecha/hora).
 *
 * Esta clase es un POJO simple con getters/setters; los DAOs la usan para
 * transferir datos entre la base y los formularios.
 */
public class CabeceraGuia {

    // Identificador único de la guía (clave natural/negocio)
    private String codigoGuia;
    private String serie;
    private String numero;
    private String codOrden;
    private String rucRemitente;
    private String rucDestinatario;
    private String dirPartida;
    private String dirLlegada;
    private String ubigeoOrigen;
    private String ubigeoDestino;
    private Double pesoTotal;
    private Integer numeroBultos;
    private Date   fechaEmision;
    private Time   horaEmision;
    private String estadoGuia;

    // Constructor vacío: útil para frameworks/DAOs que rellenan campos uno a uno
    public CabeceraGuia() {}

    public CabeceraGuia(String codigoGuia, String serie, String numero,
                        String codOrden, String rucRemitente, String rucDestinatario,
                        String dirPartida, String dirLlegada, String ubigeoOrigen, String ubigeoDestino,
                        Double pesoTotal, Integer numeroBultos,
                        Date fechaEmision, Time horaEmision,
                        String estadoGuia) {
        this.codigoGuia = codigoGuia;
        this.serie = serie;
        this.numero = numero;
        this.codOrden = codOrden;
        this.rucRemitente = rucRemitente;
        this.rucDestinatario = rucDestinatario;
        this.dirPartida = dirPartida;
        this.dirLlegada = dirLlegada;
        this.ubigeoOrigen = ubigeoOrigen;
        this.ubigeoDestino = ubigeoDestino;
        this.pesoTotal = pesoTotal;
        this.numeroBultos = numeroBultos;
        this.fechaEmision = fechaEmision;
        this.horaEmision = horaEmision;
        this.estadoGuia = estadoGuia;
    }

    public String getCodigoGuia() {
        return codigoGuia;
    }

    // setCodigoGuia(): asigna el identificador de la guía
    public void setCodigoGuia(String codigoGuia) {
        this.codigoGuia = codigoGuia;
    }

    public String getSerie() {
        return serie;
    }

    // getSerie/setSerie: serie fiscal de la guía (ej. EG07)
    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumero() {
        return numero;
    }

    // getNumero/setNumero: correlativo de la serie (formato texto para preservar ceros)
    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCodOrden() {
        return codOrden;
    }

    // codOrden: referencia a una orden de compra/servicio asociada (puede ser null)
    public void setCodOrden(String codOrden) {
        this.codOrden = codOrden;
    }

    public String getRucRemitente() {
        return rucRemitente;
    }

    // rucRemitente / rucDestinatario: claves que apuntan a registros en remitente/destinatario
    public void setRucRemitente(String rucRemitente) {
        this.rucRemitente = rucRemitente;
    }

    public String getRucDestinatario() {
        return rucDestinatario;
    }

    public void setRucDestinatario(String rucDestinatario) {
        this.rucDestinatario = rucDestinatario;
    }

    public String getDirPartida() {
        return dirPartida;
    }

    // Direcciones y ubigeos: texto libre combinado con código ubigeo para normalización
    public void setDirPartida(String dirPartida) {
        this.dirPartida = dirPartida;
    }

    public String getDirLlegada() {
        return dirLlegada;
    }

    public void setDirLlegada(String dirLlegada) {
        this.dirLlegada = dirLlegada;
    }

    public String getUbigeoOrigen() {
        return ubigeoOrigen;
    }

    // ubigeoOrigen/ubigeoDestino: códigos estándar (6 dígitos) para localizar geográficamente
    public void setUbigeoOrigen(String ubigeoOrigen) {
        this.ubigeoOrigen = ubigeoOrigen;
    }

    public String getUbigeoDestino() {
        return ubigeoDestino;
    }

    public void setUbigeoDestino(String ubigeoDestino) {
        this.ubigeoDestino = ubigeoDestino;
    }

    public Double getPesoTotal() {
        return pesoTotal;
    }

    // Peso y bultos: valores numéricos usados para cálculos y visualización
    public void setPesoTotal(Double pesoTotal) {
        this.pesoTotal = pesoTotal;
    }

    public Integer getNumeroBultos() {
        return numeroBultos;
    }

    public void setNumeroBultos(Integer numeroBultos) {
        this.numeroBultos = numeroBultos;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    // fechaEmision / horaEmision: almacenadas como java.sql.Date / Time para compatibilidad JDBC
    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Time getHoraEmision() {
        return horaEmision;
    }

    public void setHoraEmision(Time horaEmision) {
        this.horaEmision = horaEmision;
    }

    public String getEstadoGuia() {
        return estadoGuia;
    }

    // estadoGuia: texto que describe el estado actual (emitida, pendiente, etc.)
    public void setEstadoGuia(String estadoGuia) {
        this.estadoGuia = estadoGuia;
    }

    @Override
    public String toString() {
        return codigoGuia + " - " + estadoGuia;
    }
}
