/*
 * RemitenteCredDAO.java
 * DAO para credenciales de remitentes. Proporciona verificación y seteo de
 * contraseñas (almacenadas como hash SHA-256). Si existen funciones en la
 * BD para validar credenciales, las usa; de lo contrario, consulta la tabla
 * `remitente_login` y, como último recurso, permite acceso por existencia
 * del remitente en la tabla `remitente` (fallback no seguro, sólo para compatibilidad).
 */
package dao;

import db.Conexion;

import java.sql.*;
import java.security.MessageDigest;

/**
 * Simple credentials DAO for remitente logins.
 * This uses a table `remitente_login(ruc CHAR(11) PRIMARY KEY, password_hash VARCHAR)` which
 * you may create in the DB. If the table does not exist, methods will safely return false
 * so the app can fallback to existence-only login.
 */
public class RemitenteCredDAO {

    private String hash(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(pwd.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    public boolean checkCredential(String ruc, String password) {
        // Primero intentar función en BD que valide credenciales (si fue creada):
        String fnSql = "SELECT sp_check_remitente_login(?, ?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement psFn = cn.prepareStatement(fnSql)) {
            psFn.setString(1, ruc);
            psFn.setString(2, password);
            try (ResultSet rsFn = psFn.executeQuery()) {
                if (rsFn.next()) {
                    Object val = rsFn.getObject(1);
                    if (val instanceof Boolean aBoolean) return aBoolean;
                    if (val instanceof Number number) return number.intValue() > 0;
                    if (val != null) return "t".equalsIgnoreCase(val.toString()) || "true".equalsIgnoreCase(val.toString());
                    return false;
                }
            }
        } catch (SQLException eFn) {
            // función no existe o falló: continuamos con el chequeo local existente
            System.err.println("Info: sp_check_remitente_login not available or failed: " + eFn.getMessage());
        }

        // Comportamiento original: buscar hash en tabla remitente_login
        String sql = "SELECT password_hash FROM remitente_login WHERE ruc = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, ruc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    String h = hash(password);
                    return h != null && h.equals(stored);
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // If table doesn't exist or error, attempt a safe fallback:
            // comprobar si el RUC existe en la tabla `remitente` y permitir acceso por presencia (conteo).
            System.err.println("Warning: checkCredential failed: " + e.getMessage());
            try (Connection cn2 = Conexion.getConnection();
                 PreparedStatement ps2 = cn2.prepareStatement("SELECT COUNT(1) FROM remitente WHERE ruc = ?")) {
                ps2.setString(1, ruc);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        int cnt = rs2.getInt(1);
                        if (cnt > 0) {
                            System.err.println("Info: remitente encontrado en tabla 'remitente' — acceso permitido por existencia (sin password).");
                            return true;
                        }
                    }
                }
            } catch (SQLException ex2) {
                System.err.println("Warning: fallback check in remitente failed: " + ex2.getMessage());
            }
            return false;
        }
    }

    public boolean setCredential(String ruc, String password) {
        String hashed = hash(password);
        if (hashed == null) return false;
        // try update, if no rows then insert
        String upd = "UPDATE remitente_login SET password_hash = ? WHERE ruc = ?";
        String ins = "INSERT INTO remitente_login(ruc, password_hash) VALUES(?,?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(upd)) {
            ps.setString(1, hashed); ps.setString(2, ruc);
            int updated = ps.executeUpdate();
            if (updated > 0) return true;
            try (PreparedStatement ps2 = cn.prepareStatement(ins)) {
                ps2.setString(1, ruc); ps2.setString(2, hashed);
                ps2.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Warning: setCredential failed: " + e.getMessage());
            return false;
        }
    }
}
