package dao;

import db.Conexion;
import model.Vehiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {
    public boolean insertar(Vehiculo v) {
        String call = "{ call sp_insertar_vehiculo(?, ?, ?, ?, ?, ?) }";
        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, v.getPlaca());
            cs.setString(2, v.getNumeroMtc());
            cs.setString(3, v.getTipoVehiculo());
            cs.setString(4, v.getMarca());
            cs.setString(5, v.getModelo());
            // enviar carga_max como BigDecimal para coincidir con NUMERIC en el procedimiento
            if (v.getCargaMax() == 0.0) cs.setNull(6, java.sql.Types.NUMERIC);
            else cs.setBigDecimal(6, java.math.BigDecimal.valueOf(v.getCargaMax()));
            cs.execute();
            return true;
        } catch (SQLException e) {
            // Log detallado para depuración (se mostrará en la consola)
            System.err.println("Error insertar vehiculo: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState() + " ErrorCode: " + e.getErrorCode());
            e.printStackTrace(System.err);
            // Intentar fallback: INSERT directo en tabla vehiculo (ON CONFLICT DO NOTHING)
            String ins = "INSERT INTO vehiculo(placa, numero_mtc, tipo_vehiculo, marca, modelo, carga_max) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (placa) DO NOTHING";
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(ins)) {
                ps.setString(1, v.getPlaca());
                ps.setString(2, v.getNumeroMtc());
                ps.setString(3, v.getTipoVehiculo());
                ps.setString(4, v.getMarca());
                ps.setString(5, v.getModelo());
                if (v.getCargaMax() == 0.0) ps.setNull(6, java.sql.Types.NUMERIC); else ps.setBigDecimal(6, java.math.BigDecimal.valueOf(v.getCargaMax()));
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.err.println("Inserted vehiculo via fallback INSERT (rows=" + rows + ")");
                    return true;
                } else {
                    System.err.println("Fallback INSERT affected 0 rows (placa may already exist)");
                    return false;
                }
            } catch (SQLException exIns) {
                System.err.println("Error insertar vehiculo (fallback INSERT): " + exIns.getMessage());
                exIns.printStackTrace(System.err);
                return false;
            }
        }
    }

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_vehiculos()";
        String sqlFallback = "SELECT placa, numero_mtc, tipo_vehiculo, marca, modelo, carga_max FROM vehiculo";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                boolean hasPlaca = true;
                try { rs.findColumn("placa"); } catch (SQLException ex) { hasPlaca = false; }
                if (hasPlaca) {
                    while (rs.next()) {
                        Vehiculo v = new Vehiculo(
                                rs.getString("placa"),
                                rs.getString("numero_mtc"),
                                rs.getString("tipo_vehiculo"),
                                rs.getString("marca"),
                                rs.getString("modelo"),
                                rs.getObject("carga_max") == null ? 0.0 : rs.getDouble("carga_max")
                        );
                        lista.add(v);
                    }
                    return lista;
                }
            } catch (SQLException exFn) {
                // ignore and fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    Vehiculo v = new Vehiculo(
                            rs2.getString("placa"),
                            rs2.getString("numero_mtc"),
                            rs2.getString("tipo_vehiculo"),
                            rs2.getString("marca"),
                            rs2.getString("modelo"),
                            rs2.getObject("carga_max") == null ? 0.0 : rs2.getDouble("carga_max")
                    );
                    lista.add(v);
                }
            } catch (SQLException e) {
                System.err.println("Error listar vehiculos (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error obtener conexión listar vehiculos: " + e.getMessage());
        }
        return lista;
    }

    public Vehiculo buscarPorPlaca(String placa) {
        // Intentar usar la función de listado y filtrar por placa
        try {
            for (Vehiculo v : listarTodos()) if (v.getPlaca() != null && v.getPlaca().equals(placa)) return v;
        } catch (Exception ex) {
            // fallback directo
            String sql = "SELECT placa, numero_mtc, tipo_vehiculo, marca, modelo, carga_max FROM vehiculo WHERE placa = ?";
            try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, placa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Vehiculo(
                                rs.getString("placa"),
                                rs.getString("numero_mtc"),
                                rs.getString("tipo_vehiculo"),
                                rs.getString("marca"),
                                rs.getString("modelo"),
                                rs.getObject("carga_max") == null ? 0.0 : rs.getDouble("carga_max")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error buscar vehiculo por placa: " + e.getMessage());
            }
        }
        return null;
    }

    public boolean eliminar(String placa) {
        String call = "{ call sp_eliminar_vehiculo(?) }";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, placa);
            cs.execute();
            return true;
        } catch (SQLException e) {
            // Log detallado
            System.err.println("Error eliminar vehiculo (CALL): " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState() + " ErrorCode: " + e.getErrorCode());
            e.printStackTrace(System.err);

            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            // Si el procedimiento no existe, intentar DELETE directo
            if (msg.contains("no existe") && (msg.contains("proced") || msg.contains("function") || msg.contains("procedure"))) {
                try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement("DELETE FROM vehiculo WHERE placa = ?")) {
                    ps.setString(1, placa);
                    int rows = ps.executeUpdate();
                    return rows > 0;
                } catch (SQLException ex2) {
                    System.err.println("Error eliminar vehiculo (fallback DELETE): " + ex2.getMessage());
                    ex2.printStackTrace(System.err);
                    return false;
                }
            }

            // Si es violación de FK (Postgres SQLSTATE 23503) o mensaje contiene 'foreign key', informar y no intentar borrar
            if ("23503".equals(e.getSQLState()) || msg.contains("foreign key") || msg.contains("llave foranea") || msg.contains("llave foránea") || msg.contains("violat")) {
                System.err.println("No se puede eliminar vehículo: existe referencia en otras tablas (por ejemplo traslado). Elimina o actualiza las referencias primero.");
                return false;
            }

            // Por defecto, intentar DELETE directo como último recurso
            try (Connection cn3 = Conexion.getConnection(); PreparedStatement ps3 = cn3.prepareStatement("DELETE FROM vehiculo WHERE placa = ?")) {
                ps3.setString(1, placa);
                int rows = ps3.executeUpdate();
                return rows > 0;
            } catch (SQLException ex3) {
                System.err.println("Error eliminar vehiculo (fallback final): " + ex3.getMessage());
                ex3.printStackTrace(System.err);
                return false;
            }
        }
    }

    public boolean actualizar(Vehiculo v) {
        String call = "{ call sp_actualizar_vehiculo(?, ?, ?, ?, ?, ?) }";
        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, v.getPlaca());
            cs.setString(2, v.getNumeroMtc());
            cs.setString(3, v.getTipoVehiculo());
            cs.setString(4, v.getMarca());
            cs.setString(5, v.getModelo());
            cs.setDouble(6, v.getCargaMax());
            cs.execute();
            return true;
        } catch (SQLException e) {
            // Si el procedimiento no existe o falla, intentamos un UPDATE directo como fallback
            System.err.println("sp_actualizar_vehiculo failed, falling back to direct UPDATE: " + e.getMessage());
            String sql = "UPDATE vehiculo SET numero_mtc=?, tipo_vehiculo=?, marca=?, modelo=?, carga_max=? WHERE placa=?";
            try (Connection cn2 = Conexion.getConnection();
                 PreparedStatement ps = cn2.prepareStatement(sql)) {
                ps.setString(1, v.getNumeroMtc());
                ps.setString(2, v.getTipoVehiculo());
                ps.setString(3, v.getMarca());
                ps.setString(4, v.getModelo());
                ps.setDouble(5, v.getCargaMax());
                ps.setString(6, v.getPlaca());
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback UPDATE failed: " + ex2.getMessage());
                return false;
            }
        }
    }
}
