package dao;

import db.Conexion;
import model.Ubigeo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;

public class UbigeoDAO {

    public boolean insertar(Ubigeo u) {
        String call = "CALL sp_insertar_ubigeo(?, ?, ?, ?)";
        String ins = "INSERT INTO ubigeo (codigo_ubigeo, departamento, provincia, distrito) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, u.getCodigo());
            cs.setString(2, u.getDepartamento());
            cs.setString(3, u.getProvincia());
            cs.setString(4, u.getDistrito());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_insertar_ubigeo failed: " + e.getMessage());
            // fallback to direct INSERT
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(ins)) {
                ps.setString(1, u.getCodigo());
                ps.setString(2, u.getDepartamento());
                ps.setString(3, u.getProvincia());
                ps.setString(4, u.getDistrito());
                ps.executeUpdate();
                return true;
            } catch (SQLException ex2) {
                System.err.println("Fallback INSERT ubigeo failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    public boolean actualizar(Ubigeo u) {
        String call = "CALL sp_actualizar_ubigeo(?, ?, ?, ?)";
        String upd = "UPDATE ubigeo SET departamento = ?, provincia = ?, distrito = ? WHERE codigo_ubigeo = ?";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, u.getCodigo());
            cs.setString(2, u.getDepartamento());
            cs.setString(3, u.getProvincia());
            cs.setString(4, u.getDistrito());
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_actualizar_ubigeo failed: " + e.getMessage());
            // fallback to direct UPDATE
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(upd)) {
                ps.setString(1, u.getDepartamento());
                ps.setString(2, u.getProvincia());
                ps.setString(3, u.getDistrito());
                ps.setString(4, u.getCodigo());
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback UPDATE ubigeo failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    public boolean eliminar(String codigo) {
        String call = "CALL sp_eliminar_ubigeo(?)";
        String del = "DELETE FROM ubigeo WHERE codigo_ubigeo = ?";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigo);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_eliminar_ubigeo failed: " + e.getMessage());
            // fallback to direct DELETE
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(del)) {
                ps.setString(1, codigo);
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback DELETE ubigeo failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    public List<Ubigeo> listarTodos() {
        List<Ubigeo> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_ubigeos()";
        String sqlFallback = "SELECT codigo_ubigeo, departamento, provincia, distrito FROM ubigeo ORDER BY departamento, provincia, distrito";
        try (Connection cn = Conexion.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(fnSql); ResultSet rs = ps.executeQuery()) {
                boolean hasCodigo = true;
                try { rs.findColumn("codigo_ubigeo"); } catch (SQLException ex) { hasCodigo = false; }
                if (hasCodigo) {
                    while (rs.next()) {
                        lista.add(new Ubigeo(
                                rs.getString("codigo_ubigeo"),
                                rs.getString("departamento"),
                                rs.getString("provincia"),
                                rs.getString("distrito")
                        ));
                    }
                    return lista;
                }
            } catch (SQLException exFn) {
                // fallback
            }

            try (PreparedStatement ps2 = cn.prepareStatement(sqlFallback); ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    lista.add(new Ubigeo(
                            rs2.getString("codigo_ubigeo"),
                            rs2.getString("departamento"),
                            rs2.getString("provincia"),
                            rs2.getString("distrito")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("Error listar ubigeos (fallback): " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error obtener conexión listar ubigeos: " + e.getMessage());
        }
        return lista;
    }

    public Ubigeo buscarPorCodigo(String codigo) {
        String sql = "SELECT codigo_ubigeo, departamento, provincia, distrito FROM ubigeo WHERE codigo_ubigeo = ?";
        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ubigeo(
                            rs.getString("codigo_ubigeo"),
                            rs.getString("departamento"),
                            rs.getString("provincia"),
                            rs.getString("distrito")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscar ubigeo: " + e.getMessage());
        }
        return null;
    }
}
