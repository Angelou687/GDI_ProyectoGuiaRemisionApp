/*
 * RemitenteDAO.java
 * DAO para operaciones sobre remitentes (CRUD y listados). Soporta el uso
 * de funciones/procedimientos en la BD y proporciona consultas SQL como
 * fallback cuando las funciones no están disponibles.
 */
package dao;

import db.Conexion;
import model.Remitente;

import java.sql.*;

public class RemitenteDAO {

    public boolean insertar(Remitente r) {
        String call = "CALL sp_insertar_remitente(?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, r.getRuc());
            cs.setString(2, r.getNombreEmpresa());
            cs.setString(3, r.getRazonSocial());
            cs.setString(4, r.getTelefono());
            cs.setString(5, r.getEmail());
            cs.setString(6, r.getCalleDireccion());
            cs.setString(7, r.getCodigoUbigeo());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error insertar remitente: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Remitente r) {
        String call = "CALL sp_actualizar_remitente(?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, r.getRuc());
            cs.setString(2, r.getNombreEmpresa());
            cs.setString(3, r.getRazonSocial());
            cs.setString(4, r.getTelefono());
            cs.setString(5, r.getEmail());
            cs.setString(6, r.getCalleDireccion());
            cs.setString(7, r.getCodigoUbigeo());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error actualizar remitente: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String ruc) {
        String call = "CALL sp_eliminar_remitente(?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, ruc);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error eliminar remitente: " + e.getMessage());
            return false;
        }
    }

    public boolean existe(String ruc) {
        String sql = "SELECT 1 FROM remitente WHERE ruc = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ruc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error comprobar remitente: " + e.getMessage());
            return false;
        }
    }

    public Remitente buscarPorRuc(String ruc) {
        String fnSql = "SELECT * FROM sp_buscar_remitente(?)";
        String sqlFallback = "SELECT ruc, nombre_empresa, razon_social, telefono, email, calle_direccion, codigo_ubigeo FROM remitente WHERE ruc = ?";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql)) {
                ps.setString(1, ruc);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Remitente r = new Remitente();
                        r.setRuc(rs.getString("ruc"));
                        r.setNombreEmpresa(rs.getString("nombre_empresa"));
                        r.setRazonSocial(rs.getString("razon_social"));
                        r.setTelefono(rs.getString("telefono"));
                        r.setEmail(rs.getString("email"));
                        r.setCalleDireccion(rs.getString("calle_direccion"));
                        r.setCodigoUbigeo(rs.getString("codigo_ubigeo"));
                        return r;
                    }
                }
            } catch (SQLException exFn) {
                // fallback al SELECT directo
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback)) {
                ps2.setString(1, ruc);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        Remitente r = new Remitente();
                        r.setRuc(rs2.getString("ruc"));
                        r.setNombreEmpresa(rs2.getString("nombre_empresa"));
                        r.setRazonSocial(rs2.getString("razon_social"));
                        r.setTelefono(rs2.getString("telefono"));
                        r.setEmail(rs2.getString("email"));
                        r.setCalleDireccion(rs2.getString("calle_direccion"));
                        r.setCodigoUbigeo(rs2.getString("codigo_ubigeo"));
                        return r;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscar remitente: " + e.getMessage());
        }
        return null;
    }

    public java.util.List<String> listarRucs() {
        java.util.List<String> out = new java.util.ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_remitentes()";
        String sqlFallback = "SELECT ruc, nombre_empresa FROM remitente ORDER BY nombre_empresa";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString("ruc"));
                }
                return out;
            } catch (SQLException exFn) {
                // fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) out.add(rs2.getString("ruc"));
            } catch (SQLException e) {
                System.err.println("Error listarRucs remitente (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listarRucs: " + e.getMessage());
        }
        return out;
    }
}
