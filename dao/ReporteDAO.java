/*
 * ReporteDAO.java
 * DAO utilitario para ejecutar consultas dinámicas y procedimientos que
 * devuelven filas. Retorna resultados en `DefaultTableModel` listos para
 * mostrar en tablas Swing. Maneja fallbacks y captura errores SQL.
 */
package dao;

import db.Conexion;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class ReporteDAO {

    public DefaultTableModel ejecutarConsulta(String sql) {
        DefaultTableModel model = new DefaultTableModel();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnas = meta.getColumnCount();

            for (int i = 1; i <= columnas; i++) {
                model.addColumn(meta.getColumnLabel(i));
            }

            while (rs.next()) {
                Object[] fila = new Object[columnas];
                for (int i = 0; i < columnas; i++) {
                    fila[i] = rs.getObject(i + 1);
                }
                model.addRow(fila);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar reporte: " + e.getMessage());
        }

        return model;
    }

    public DefaultTableModel ejecutarConsultaConParametro(String sql, String param) {
        DefaultTableModel model = new DefaultTableModel();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                for (int i = 1; i <= columnas; i++) {
                    model.addColumn(meta.getColumnLabel(i));
                }

                while (rs.next()) {
                    Object[] fila = new Object[columnas];
                    for (int i = 0; i < columnas; i++) {
                        fila[i] = rs.getObject(i + 1);
                    }
                    model.addRow(fila);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar reporte con parametro: " + e.getMessage());
        }

        return model;
    }

    // Ejecuta una "función/procedimiento" que devuelve filas: usamos SELECT * FROM func()
    public DefaultTableModel ejecutarSP(String spName) {
        DefaultTableModel model = new DefaultTableModel();
        // spName puede venir con o sin paréntesis; normalizamos:
        String sql;
        if (spName.endsWith("()")) sql = "SELECT * FROM " + spName.substring(0, spName.length()-2) + "()";
        else sql = "SELECT * FROM " + spName + "()";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnas = meta.getColumnCount();
            for (int i = 1; i <= columnas; i++) model.addColumn(meta.getColumnLabel(i));

            while (rs.next()) {
                Object[] fila = new Object[columnas];
                for (int i = 0; i < columnas; i++) fila[i] = rs.getObject(i + 1);
                model.addRow(fila);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar SP " + spName + ": " + e.getMessage());
        }

        return model;
    }

    // Ejecuta función/procedimiento con 1 parámetro que devuelve filas
    public DefaultTableModel ejecutarSPConParametro(String spName, String param) {
        DefaultTableModel model = new DefaultTableModel();
        String sql = "SELECT * FROM " + spName + "(?)";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();
                for (int i = 1; i <= columnas; i++) model.addColumn(meta.getColumnLabel(i));

                while (rs.next()) {
                    Object[] fila = new Object[columnas];
                    for (int i = 0; i < columnas; i++) fila[i] = rs.getObject(i + 1);
                    model.addRow(fila);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar SP con param " + spName + ": " + e.getMessage());
        }

        return model;
    }
}
