/*
 * DestinatarioDAO.java
 * DAO responsable de las operaciones CRUD sobre la tabla `destinatario`.
 * Estrategia: intentar llamar a procedimientos/funciones almacenadas (SP/func)
 * cuando estén disponibles; en caso de error, usar consultas SQL directas
 * como fallback para mantener compatibilidad.
 */
package dao;

import db.Conexion;
import model.Destinatario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinatarioDAO {

    // listar usando función/procedimiento que devuelve filas en Postgres:
    public List<Destinatario> listarTodos() {
        List<Destinatario> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_destinatarios()"; // Postgres: función que devuelve SETOF

        // Primero intentar la función almacenada (si existe)
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(fnSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Destinatario d = new Destinatario();
                d.setRuc(rs.getString("ruc"));
                d.setNombre(rs.getString("nombre"));
                d.setNumeroTelefono(rs.getString("numero_telefono"));
                d.setCalleDireccion(rs.getString("calle_direccion"));
                d.setCodigoUbigeo(rs.getString("codigo_ubigeo"));
                d.setGmail(rs.getString("gmail"));
                lista.add(d);
            }

            return lista;

        } catch (SQLException e) {
            // Si la función no existe o falla, caer en un SELECT directo como fallback
            System.out.println("sp_listar_destinatarios() failed, intentando fallback SELECT: " + e.getMessage());
        }

        // Fallback: leer directamente de la tabla destinatario (filtrar eliminados)
        String sql = "SELECT ruc, nombre, numero_telefono, calle_direccion, codigo_ubigeo, gmail FROM destinatario WHERE eliminado = false ORDER BY nombre";
        try (Connection cn2 = Conexion.getConnection();
             PreparedStatement ps2 = cn2.prepareStatement(sql);
             ResultSet rs2 = ps2.executeQuery()) {

            while (rs2.next()) {
                Destinatario d = new Destinatario();
                d.setRuc(rs2.getString("ruc"));
                d.setNombre(rs2.getString("nombre"));
                d.setNumeroTelefono(rs2.getString("numero_telefono"));
                d.setCalleDireccion(rs2.getString("calle_direccion"));
                d.setCodigoUbigeo(rs2.getString("codigo_ubigeo"));
                d.setGmail(rs2.getString("gmail"));
                lista.add(d);
            }

        } catch (SQLException ex2) {
            System.out.println("Error al listar destinatarios (fallback): " + ex2.getMessage());
        }

        return lista;
    }

    // insertar: usar CALL si es procedimiento (Postgres 11+), o SELECT si es función; aquí usamos CALL
    public boolean insertar(Destinatario d) {
        String call = "CALL sp_insertar_destinatario(?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {

            cs.setString(1, d.getRuc());
            cs.setString(2, d.getNombre());
            cs.setString(3, d.getNumeroTelefono());
            cs.setString(4, d.getCalleDireccion());
            cs.setString(5, d.getCodigoUbigeo());
            cs.setString(6, d.getGmail());

            cs.execute();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al insertar destinatario (CALL): " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Destinatario d) {
        String call = "CALL sp_actualizar_destinatario(?, ?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {

            // Validar ubigeo
            String codigoUbigeo = d.getCodigoUbigeo();
            if (codigoUbigeo != null && !codigoUbigeo.trim().isEmpty()) {
                if (!ubigeoExiste(cn, codigoUbigeo)) {
                    System.out.println("Error al actualizar destinatario: el codigo_ubigeo '" + codigoUbigeo + "' no existe en la tabla ubigeo.");
                    return false;
                }
            }

            cs.setString(1, d.getRuc());
            cs.setString(2, d.getNombre());
            cs.setString(3, d.getNumeroTelefono());
            cs.setString(4, d.getCalleDireccion());
            cs.setString(5, d.getCodigoUbigeo());
            cs.setString(6, d.getGmail());

            cs.execute();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al actualizar destinatario (CALL): " + e.getMessage());
            return false;
        }
    }

    public boolean ubigeoExiste(String codigoUbigeo) {
        if (codigoUbigeo == null || codigoUbigeo.trim().isEmpty()) return false;
        try (Connection cn = Conexion.getConnection()) {
            return ubigeoExiste(cn, codigoUbigeo);
        } catch (SQLException e) {
            System.out.println("Error comprobando ubigeo: " + e.getMessage());
            return false;
        }
    }

    private boolean ubigeoExiste(Connection cn, String codigoUbigeo) throws SQLException {
        String sql = "SELECT 1 FROM ubigeo WHERE codigo_ubigeo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigoUbigeo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean eliminar(String ruc) {
        // Intentar procedimiento almacenado primero; si no existe o falla, realizar soft-delete
        String call = "CALL sp_eliminar_destinatario(?)";
        String sql = "UPDATE destinatario SET eliminado = true WHERE ruc = ?";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, ruc);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_eliminar_destinatario failed: " + e.getMessage());
            // fallback a soft-delete
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(sql)) {
                ps.setString(1, ruc);
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback soft-delete destinatario failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    // Recuperar destinatario eliminado
    public boolean recuperar(String ruc) {
        String sql = "UPDATE destinatario SET eliminado = false WHERE ruc = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ruc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al recuperar destinatario: " + e.getMessage());
            return false;
        }
    }
}
