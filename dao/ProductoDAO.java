/*
 * ProductoDAO.java
 * DAO para productos: creación, actualización, eliminación y listados.
 * Intenta ejecutar SPs (CALL) y, si fallan, aplica consultas/operaciones
 * SQL directas como fallback para mantener compatibilidad.
 */
package dao;

import db.Conexion;
import model.Producto;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public boolean crear(Producto p) {
        String call = "CALL sp_insertar_producto(?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, p.getCodigoProducto());
            cs.setString(2, p.getNombreProducto());
            cs.setBigDecimal(3, BigDecimal.valueOf(p.getPrecioBase()));
            cs.setString(4, p.getUnidadMedida());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error crear producto: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Producto p) {
        String call = "CALL sp_actualizar_producto(?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, p.getCodigoProducto());
            cs.setString(2, p.getNombreProducto());
            cs.setBigDecimal(3, BigDecimal.valueOf(p.getPrecioBase()));
            cs.setString(4, p.getUnidadMedida());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String codigo) {
        String call = "CALL sp_eliminar_producto(?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error eliminar producto: " + e.getMessage());
            return false;
        }
    }

    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_productos()";
        String sqlFallback = "SELECT codigo_producto, nombre_producto, precio_base, unidad_medida FROM producto ORDER BY nombre_producto";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql);
                 ResultSet rs = ps.executeQuery()) {
                // validar columnas
                boolean hasCodigo = true;
                boolean hasPrecio = true;
                try { rs.findColumn("codigo_producto"); } catch (SQLException ex) { hasCodigo = false; }
                try { rs.findColumn("precio_base"); } catch (SQLException ex) { hasPrecio = false; }

                if (hasCodigo && hasPrecio) {
                    while (rs.next()) {
                        Producto p = new Producto(
                                rs.getString("codigo_producto"),
                                rs.getString("nombre_producto"),
                                rs.getObject("precio_base") == null ? 0.0 : rs.getDouble("precio_base"),
                                rs.getString("unidad_medida")
                        );
                        lista.add(p);
                    }
                    return lista;
                }
                // si no contiene las columnas esperadas, caerá al fallback
            } catch (SQLException exFn) {
                // ignorar y usar fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback);
                 ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    Producto p = new Producto(
                            rs2.getString("codigo_producto"),
                            rs2.getString("nombre_producto"),
                            rs2.getObject("precio_base") == null ? 0.0 : rs2.getDouble("precio_base"),
                            rs2.getString("unidad_medida")
                    );
                    lista.add(p);
                }
            } catch (SQLException e) {
                System.err.println("Error listar productos (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listar productos: " + e.getMessage());
        }
        return lista;
    }

    public DefaultTableModel listarModeloTabla() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Código");
        model.addColumn("Nombre");
        model.addColumn("Precio base");
        model.addColumn("Unidad");
        for (Producto p : listarTodos()) {
            model.addRow(new Object[]{p.getCodigoProducto(), p.getNombreProducto(), String.format("%.2f", p.getPrecioBase()), p.getUnidadMedida()});
        }
        return model;
    }

    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) return null;
        try {
            for (Producto p : listarTodos()) {
                if (codigo.equals(p.getCodigoProducto())) return p;
            }
        } catch (Exception e) {
            System.err.println("Error buscar producto: " + e.getMessage());
        }
        return null;
    }
}
