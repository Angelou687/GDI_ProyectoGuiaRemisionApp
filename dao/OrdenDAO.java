/*
 * OrdenDAO.java
 * DAO para órdenes de pago. Contiene utilidades para mapear estados entre
 * la representación en BD y la mostrada en la UI, y métodos CRUD que
 * intentan usar SPs con fallbacks seguros a SQL directo.
 */
package dao;

import db.Conexion;
import model.OrdenDePago;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdenDAO {

    private static final java.util.Map<Character,String> ESTADO_MAP = java.util.Map.of(
            'P', "Pendiente",
            'E', "Emitida",
            'C', "Confirmada"
    );

    private String dbEstadoToDisplay(String dbVal) {
        if (dbVal == null) return "";
        String v = dbVal.trim();
        if (v.length() == 0) return "";
        // If stored as single-char code, expand
        if (v.length() == 1) {
            char c = Character.toUpperCase(v.charAt(0));
            String r = ESTADO_MAP.get(c);
            return r != null ? r : String.valueOf(c);
        }
        // If stored as word, try to normalize
        String lower = v.toLowerCase();
        if (lower.contains("emit")) return "Emitida";
        if (lower.contains("pend")) return "Pendiente";
        if (lower.contains("confirm")) return "Confirmada";
        // default: capitalize first letter
        return v.substring(0,1).toUpperCase() + v.substring(1);
    }

    private String displayEstadoToDbCode(String display) {
        if (display == null) return null;
        String d = display.trim().toLowerCase();
        if (d.isEmpty()) return null;
        if (d.contains("pend")) return "P";
        if (d.contains("emit")) return "E";
        if (d.contains("confirm")) return "C";
        return display.substring(0,1).toUpperCase();
    }


    public boolean crear(OrdenDePago o) {
        String call = "CALL sp_crear_orden_de_pago(?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, o.getCodigoOrden());
            if (o.getFecha() == null) {
                cs.setNull(2, Types.DATE);
            } else {
                cs.setObject(2, o.getFecha(), Types.DATE);
            }
            cs.setString(3, o.getRucCliente());
            // map display estado to DB code (P=Pendiente, E=Emitida, C=Confirmada)
            String code = displayEstadoToDbCode(o.getEstado());
            if (code == null) cs.setNull(4, Types.CHAR); else cs.setString(4, code);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error crear orden: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(OrdenDePago o) {
        String call = "CALL sp_actualizar_orden_de_pago(?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, o.getCodigoOrden());
            if (o.getFecha() == null) {
                cs.setNull(2, Types.DATE);
            } else {
                cs.setObject(2, o.getFecha(), Types.DATE);
            }
            cs.setString(3, o.getRucCliente());
            String code = displayEstadoToDbCode(o.getEstado());
            if (code == null) cs.setNull(4, Types.CHAR); else cs.setString(4, code);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error actualizar orden: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String codigoOrden) {
        String call = "{ call sp_eliminar_orden_de_pago(?) }";
        String fnSelect = "SELECT sp_eliminar_orden_de_pago(?)";
        // 1) intentar CALL
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigoOrden);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_eliminar_orden_de_pago CALL failed: " + e.getMessage());
            // 2) intentar como función (SELECT)
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement psFn = cn2.prepareStatement(fnSelect)) {
                psFn.setString(1, codigoOrden);
                psFn.execute();
                return true;
            } catch (SQLException exFn) {
                System.err.println("sp_eliminar_orden_de_pago SELECT failed: " + exFn.getMessage());
            }

            // 3) fallback manual: eliminar detalles y luego la orden
            try {
                DetalleOrdenDAO detDao = new DetalleOrdenDAO();
                boolean okDet = detDao.eliminarPorOrden(codigoOrden);
                if (!okDet) System.err.println("Warning: eliminarPorOrden returned false for " + codigoOrden);
            } catch (Exception exDet) {
                System.err.println("Error eliminando detalles de orden en fallback: " + exDet.getMessage());
            }

            String delSql = "DELETE FROM orden_de_pago WHERE codigo_orden = ?";
            try (Connection cn3 = Conexion.getConnection(); PreparedStatement ps = cn3.prepareStatement(delSql)) {
                ps.setString(1, codigoOrden);
                int rows = ps.executeUpdate();
                return rows > 0;
            } catch (SQLException ex3) {
                System.err.println("Fallback DELETE orden_de_pago failed: " + ex3.getMessage());
                return false;
            }
        }
    }

    public List<OrdenDePago> listarTodos() {
        List<OrdenDePago> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_ordenes()";
        String sqlFallback = "SELECT codigo_orden, fecha, ruc_cliente, estado FROM orden_de_pago ORDER BY fecha DESC";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                boolean hasCodigo = true;
                try { rs.findColumn("codigo_orden"); } catch (SQLException ex) { hasCodigo = false; }
                if (hasCodigo) {
                    while (rs.next()) {
                        String rawEstado = rs.getString("estado");
                        String displayEstado = dbEstadoToDisplay(rawEstado);
                        OrdenDePago o = new OrdenDePago(
                                rs.getString("codigo_orden"),
                                rs.getDate("fecha"),
                                rs.getString("ruc_cliente"),
                                displayEstado
                        );
                        lista.add(o);
                    }
                    return lista;
                }
            } catch (SQLException exFn) {
                // fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    String rawEstado = rs2.getString("estado");
                    String displayEstado = dbEstadoToDisplay(rawEstado);
                    OrdenDePago o = new OrdenDePago(
                            rs2.getString("codigo_orden"),
                            rs2.getDate("fecha"),
                            rs2.getString("ruc_cliente"),
                            displayEstado
                    );
                    lista.add(o);
                }
            } catch (SQLException e) {
                System.err.println("Error listar ordenes (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listar ordenes: " + e.getMessage());
        }
        return lista;
    }

    public OrdenDePago buscarPorCodigo(String codigo) {
        String sql = "SELECT codigo_orden, fecha, ruc_cliente, estado FROM orden_de_pago WHERE codigo_orden = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String raw = rs.getString("estado");
                    String display = dbEstadoToDisplay(raw);
                    return new OrdenDePago(
                            rs.getString("codigo_orden"),
                            rs.getDate("fecha"),
                            rs.getString("ruc_cliente"),
                            display
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscar orden: " + e.getMessage());
        }
        return null;
    }

    // Devuelve un DefaultTableModel para mostrar en tablas si se necesita
    public DefaultTableModel listarModeloTabla() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Código");
        model.addColumn("Fecha");
        model.addColumn("RUC Cliente");
        model.addColumn("Estado");
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (OrdenDePago o : listarTodos()) {
            String fechaStr = "";
            if (o.getFecha() != null) {
                try {
                    fechaStr = o.getFecha().toLocalDate().format(fmt);
                } catch (Exception ex) {
                    fechaStr = o.getFecha().toString();
                }
            }
            model.addRow(new Object[]{o.getCodigoOrden(), fechaStr, o.getRucCliente(), o.getEstado()});
        }
        return model;
    }
}
