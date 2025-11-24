/*
 * TrasladoDAO.java
 * DAO para gestionar registros de traslado (registro, actualización,
 * listados y eliminación). Intenta usar SPs y, si no existen, efectúa
 * operaciones SQL directas como fallback.
 */
package dao;

import db.Conexion;
import model.Traslado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrasladoDAO {

    // Actualizar traslado existente
    public boolean actualizarTraslado(Traslado t) {
        String call = "CALL sp_actualizar_traslado(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {

            cs.setString(1, t.getCodigoTraslado());
            cs.setString(2, t.getCodigoGuia());
            cs.setString(3, t.getPlaca());
            cs.setString(4, t.getLicencia());
            cs.setTimestamp(5, t.getFechaInicio());
            if (t.getFechaFin() != null) {
                cs.setTimestamp(6, t.getFechaFin());
            } else {
                cs.setNull(6, java.sql.Types.TIMESTAMP);
            }
            cs.setString(7, t.getEstadoTraslado());
            cs.setString(8, t.getObservaciones());

            cs.execute();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al actualizar traslado (CALL): " + e.getMessage());
            return false;
        }
    }

    public boolean registrarTraslado(Traslado t) {
        String call = "CALL sp_registrar_traslado(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {

            cs.setString(1, t.getCodigoTraslado());
            cs.setString(2, t.getCodigoGuia());
            cs.setString(3, t.getPlaca());
            cs.setString(4, t.getLicencia());
            cs.setTimestamp(5, t.getFechaInicio());
            if (t.getFechaFin() != null) {
                cs.setTimestamp(6, t.getFechaFin());
            } else {
                cs.setNull(6, java.sql.Types.TIMESTAMP);
            }
            cs.setString(7, t.getEstadoTraslado());
            cs.setString(8, t.getObservaciones());

            cs.execute();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al registrar traslado (CALL): " + e.getMessage());
            return false;
        }
    }

    // listar usando función en Postgres que devuelve SETOF
    public List<Traslado> listarTodos() {
        List<Traslado> lista = new ArrayList<>();
        String sql = "SELECT * FROM sp_listar_traslados()";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Traslado t = new Traslado();
                t.setCodigoTraslado(rs.getString("codigo_traslado"));
                t.setCodigoGuia(rs.getString("codigo_guia"));
                t.setPlaca(rs.getString("placa"));
                t.setLicencia(rs.getString("licencia"));
                t.setFechaInicio(rs.getTimestamp("fecha_inicio"));
                t.setFechaFin(rs.getTimestamp("fecha_fin"));
                t.setEstadoTraslado(rs.getString("estado_traslado"));
                t.setObservaciones(rs.getString("observaciones"));
                lista.add(t);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar traslados: " + e.getMessage());
        }

        return lista;
    }

    // Eliminar traslado por código (hard-delete)
    public boolean eliminarTraslado(String codigoTraslado) {
        // Preferir llamar al procedimiento almacenado si existe; si falla, fallback a DELETE directo
        String call = "CALL sp_eliminar_traslado(?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigoTraslado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            // fallback a DELETE directo
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement("DELETE FROM traslado WHERE codigo_traslado = ?")) {
                ps.setString(1, codigoTraslado);
                int rows = ps.executeUpdate();
                return rows > 0;
            } catch (SQLException ex2) {
                System.out.println("Error al eliminar traslado (fallback): " + ex2.getMessage());
                return false;
            }
        }
    }

    /**
     * Obtiene el último traslado asociado a una guía (ordenado por fecha_inicio desc).
     * Este método lee directamente la tabla `traslado` como fallback cuando no existen funciones.
     */
    public Traslado obtenerPorGuia(String codigoGuia) {
        String sql = "SELECT codigo_traslado, codigo_guia, placa, licencia, fecha_inicio, fecha_fin, estado_traslado, observaciones " +
                     "FROM traslado WHERE codigo_guia = ? ORDER BY fecha_inicio DESC LIMIT 1";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigoGuia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Traslado t = new Traslado();
                    t.setCodigoTraslado(rs.getString("codigo_traslado"));
                    t.setCodigoGuia(rs.getString("codigo_guia"));
                    t.setPlaca(rs.getString("placa"));
                    t.setLicencia(rs.getString("licencia"));
                    t.setFechaInicio(rs.getTimestamp("fecha_inicio"));
                    t.setFechaFin(rs.getTimestamp("fecha_fin"));
                    t.setEstadoTraslado(rs.getString("estado_traslado"));
                    t.setObservaciones(rs.getString("observaciones"));
                    return t;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obtener traslado por guia: " + e.getMessage());
        }
        return null;
    }
}
