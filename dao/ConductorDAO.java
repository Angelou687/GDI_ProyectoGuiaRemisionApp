/*
 * ConductorDAO.java
 * DAO para operaciones CRUD sobre conductores. Usa CALL a SPs cuando
 * estén disponibles y provee fallbacks a SQL directo en caso de error.
 */
package dao;

import db.Conexion;
import model.Conductor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConductorDAO {

    public boolean insertar(Conductor c) {
        String call = "CALL sp_insertar_conductor(?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, c.getLicencia());
            cs.setString(2, c.getDni());
            cs.setString(3, c.getNombre());
            cs.setString(4, c.getTelefono());
            if (c.getFechaVencimiento() == null) cs.setNull(5, Types.DATE); else cs.setObject(5, c.getFechaVencimiento(), Types.DATE);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error insertar conductor: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Conductor c) {
        String call = "CALL sp_actualizar_conductor(?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, c.getLicencia());
            cs.setString(2, c.getDni());
            cs.setString(3, c.getNombre());
            cs.setString(4, c.getTelefono());
            if (c.getFechaVencimiento() == null) cs.setNull(5, Types.DATE); else cs.setObject(5, c.getFechaVencimiento(), Types.DATE);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error actualizar conductor: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String licencia) {
        String call = "CALL sp_eliminar_conductor(?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, licencia);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error eliminar conductor: " + e.getMessage());
            return false;
        }
    }

    public List<Conductor> listarTodos() {
        List<Conductor> out = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_conductores()";
        String sqlFallback = "SELECT licencia, dni, nombre, telefono, fecha_vencimiento_licencia FROM conductor ORDER BY nombre";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                boolean hasLic = true;
                try { rs.findColumn("licencia"); } catch (SQLException ex) { hasLic = false; }
                if (hasLic) {
                    while (rs.next()) {
                        Conductor c = new Conductor(
                                rs.getString("licencia"),
                                rs.getString("dni"),
                                rs.getString("nombre"),
                                rs.getString("telefono"),
                                rs.getDate("fecha_vencimiento_licencia")
                        );
                        out.add(c);
                    }
                    return out;
                }
            } catch (SQLException exFn) {
                // fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    Conductor c = new Conductor(
                            rs2.getString("licencia"),
                            rs2.getString("dni"),
                            rs2.getString("nombre"),
                            rs2.getString("telefono"),
                            rs2.getDate("fecha_vencimiento_licencia")
                    );
                    out.add(c);
                }
            } catch (SQLException e) {
                System.err.println("Error listar conductores (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listar conductores: " + e.getMessage());
        }
        return out;
    }

    public List<String> listarLicencias() {
        List<String> out = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_conductores()";
        String sqlFallback = "SELECT licencia FROM conductor ORDER BY nombre";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("licencia"));
                return out;
            } catch (SQLException exFn) {
                // fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) out.add(rs2.getString("licencia"));
            } catch (SQLException e) {
                System.err.println("Error listar licencias (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listar licencias: " + e.getMessage());
        }
        return out;
    }

    public Conductor buscarPorLicencia(String licencia) {
        String sql = "SELECT licencia, dni, nombre, telefono, fecha_vencimiento_licencia FROM conductor WHERE licencia = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, licencia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Conductor(
                            rs.getString("licencia"),
                            rs.getString("dni"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getDate("fecha_vencimiento_licencia")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscar conductor: " + e.getMessage());
        }
        return null;
    }
}
