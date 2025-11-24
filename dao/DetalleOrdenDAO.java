package dao;

import db.Conexion;
import model.DetalleOrden;

import javax.swing.table.DefaultTableModel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleOrdenDAO {

    public boolean agregarDetalle(DetalleOrden d) {
        String call = "CALL sp_agregar_detalle_orden(?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, d.getCodigoOrden());
            cs.setString(2, d.getCodigoProducto());
            // Enviar NUMERIC/DECIMAL como BigDecimal para coincidir con la firma del procedimiento
            if (d.getCantidad() != null) cs.setBigDecimal(3, d.getCantidad()); else cs.setNull(3, java.sql.Types.NUMERIC);
            if (d.getPrecioUnitario() != null) cs.setBigDecimal(4, d.getPrecioUnitario()); else cs.setNull(4, java.sql.Types.NUMERIC);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error agregar detalle: " + e.getMessage());
            return false;
        }
    }

    public List<DetalleOrden> listarPorOrden(String codigoOrden) {
        List<DetalleOrden> lista = new ArrayList<>();
        // Intentar usar la función/ reporte sp_reporte_detalle_orden(p_codigo_orden)
        String fnSql = "SELECT * FROM sp_reporte_detalle_orden(?)";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(fnSql)) {
            ps.setString(1, codigoOrden);
            try (ResultSet rs = ps.executeQuery()) {
                int item = 1;
                while (rs.next()) {
                    DetalleOrden d = new DetalleOrden();
                    // La función devuelve: codigo_orden, codigo_producto, nombre_producto, cantidad, precio_unitario, subtotal
                    d.setNumeroItem(item++);
                    d.setCodigoOrden(rs.getString("codigo_orden"));
                    d.setCodigoProducto(rs.getString("codigo_producto"));
                    d.setCantidad(rs.getBigDecimal("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    lista.add(d);
                }
            }
            return lista;
        } catch (SQLException e) {
            // fallback: consulta directa sobre detalle_orden (mantener compatibilidad)
            String sql = "SELECT numero_item, codigo_orden, codigo_producto, cantidad, precio_unitario, subtotal FROM detalle_orden WHERE codigo_orden = ? ORDER BY numero_item";
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps2 = cn2.prepareStatement(sql)) {
                ps2.setString(1, codigoOrden);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        DetalleOrden d = new DetalleOrden();
                        d.setNumeroItem(rs2.getInt("numero_item"));
                        d.setCodigoOrden(rs2.getString("codigo_orden"));
                        d.setCodigoProducto(rs2.getString("codigo_producto"));
                        d.setCantidad(rs2.getBigDecimal("cantidad"));
                        d.setPrecioUnitario(rs2.getBigDecimal("precio_unitario"));
                        d.setSubtotal(rs2.getBigDecimal("subtotal"));
                        lista.add(d);
                    }
                }
            } catch (SQLException ex2) {
                System.err.println("Error listar detalle orden (fallback): " + ex2.getMessage());
            }
        }
        return lista;
    }

    public DefaultTableModel listarModeloTablaPorOrden(String codigoOrden) {
        DefaultTableModel model = new DefaultTableModel();
        // columnas en el orden que la UI espera
        model.addColumn("Código");
        model.addColumn("Nombre");
        model.addColumn("Cantidad");
        model.addColumn("Precio unit.");
        model.addColumn("Subtotal");

        java.util.List<DetalleOrden> list = listarPorOrden(codigoOrden);
        dao.ProductoDAO prodDao = new dao.ProductoDAO();
        for (DetalleOrden d : list) {
            String codigoProd = d.getCodigoProducto();
            String nombre = codigoProd;
            try {
                model.Producto prod = prodDao.buscarPorCodigo(codigoProd);
                if (prod != null && prod.getNombreProducto() != null) nombre = prod.getNombreProducto();
            } catch (Exception ex) {
                // ignore, usar codigo como nombre
            }
            model.addRow(new Object[]{codigoProd, nombre, d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()});
        }
        return model;
    }

    public boolean eliminarPorOrden(String codigoOrden) {
        String call = "CALL sp_eliminar_detalle_orden(?)";
        String fnSelect = "SELECT sp_eliminar_detalle_orden(?)";
        String sql = "DELETE FROM detalle_orden WHERE codigo_orden = ?";
        // 1) intentar CALL
        try (java.sql.Connection cn = Conexion.getConnection(); java.sql.CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigoOrden);
            cs.execute();
            return true;
        } catch (java.sql.SQLException e) {
            System.err.println("sp_eliminar_detalle_orden CALL failed: " + e.getMessage());
            // 2) intentar como función SELECT sp_eliminar_detalle_orden(?)
            try (java.sql.Connection cn2 = Conexion.getConnection(); java.sql.PreparedStatement psFn = cn2.prepareStatement(fnSelect)) {
                psFn.setString(1, codigoOrden);
                psFn.execute();
                // Si la función existe y se ejecuta, consideramos éxito
                return true;
            } catch (java.sql.SQLException exFn) {
                System.err.println("sp_eliminar_detalle_orden SELECT failed: " + exFn.getMessage());
            }

            // 3) fallback directo: DELETE
            try (java.sql.Connection cn3 = Conexion.getConnection(); java.sql.PreparedStatement ps = cn3.prepareStatement(sql)) {
                ps.setString(1, codigoOrden);
                ps.executeUpdate();
                return true;
            } catch (java.sql.SQLException ex2) {
                System.err.println("Fallback DELETE detalle_orden failed: " + ex2.getMessage());
                return false;
            }
        }
    }
}
