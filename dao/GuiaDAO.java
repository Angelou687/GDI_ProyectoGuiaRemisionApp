/*
 * GuiaDAO.java
 * DAO para operaciones relacionadas con guías de remisión y su detalle.
 * - Intenta usar funciones/procedimientos del lado DB cuando existan.
 * - Provee múltiples niveles de fallback (CALL/SELECT/INSERT directo)
 *   para mantener operativa la aplicación aunque falten SPs.
 */
package dao;

import db.Conexion;
import model.CabeceraGuia;
import model.DetalleGuia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public class GuiaDAO {

    public List<CabeceraGuia> listarTodas() {
        List<CabeceraGuia> lista = new ArrayList<>();
        String fnSql = "SELECT * FROM sp_listar_guias()";
        // Fallback SQL (consulta directa) si la función no devuelve las columnas que la app espera
        String sqlWithEliminado = "SELECT codigo_guia, serie, numero, cod_orden, ruc_remitente, ruc_destinatario, " +
                "dir_partida, dir_llegada, ubigeo_origen, ubigeo_destino, peso_total, numero_bultos, " +
                "fecha_emision, hora_emision, estado_guia " +
                "FROM cabecera_guia WHERE coalesce(eliminado,false) = false";
        String sqlFallback = "SELECT codigo_guia, serie, numero, cod_orden, ruc_remitente, ruc_destinatario, " +
                "dir_partida, dir_llegada, ubigeo_origen, ubigeo_destino, peso_total, numero_bultos, " +
                "fecha_emision, hora_emision, estado_guia " +
                "FROM cabecera_guia";

        try (Connection cn = Conexion.getConnection()) {
            // Intentar función primero
            try (PreparedStatement ps = cn.prepareStatement(fnSql);
                 ResultSet rs = ps.executeQuery()) {
                // Comprobar si la función devuelve las columnas que necesitamos (ruc_remitente, hora_emision)
                boolean hasRucRem = true;
                boolean hasHora = true;
                try { rs.findColumn("ruc_remitente"); } catch (SQLException ex) { hasRucRem = false; }
                try { rs.findColumn("hora_emision"); } catch (SQLException ex) { hasHora = false; }

                if (hasRucRem && hasHora) {
                    while (rs.next()) {
                        CabeceraGuia g = new CabeceraGuia();
                        g.setCodigoGuia(rs.getString("codigo_guia"));
                        g.setSerie(rs.getString("serie"));
                        g.setNumero(rs.getString("numero"));
                        g.setCodOrden(rs.getString("cod_orden"));
                        g.setRucRemitente(rs.getString("ruc_remitente"));
                        g.setRucDestinatario(rs.getString("ruc_destinatario"));
                        g.setDirPartida(rs.getString("dir_partida"));
                        g.setDirLlegada(rs.getString("dir_llegada"));
                        g.setUbigeoOrigen(rs.getString("ubigeo_origen"));
                        g.setUbigeoDestino(rs.getString("ubigeo_destino"));
                        g.setPesoTotal(rs.getObject("peso_total") == null ? 0.0 : rs.getDouble("peso_total"));
                        g.setNumeroBultos(rs.getObject("numero_bultos") == null ? 0 : rs.getInt("numero_bultos"));
                        g.setFechaEmision(rs.getDate("fecha_emision"));
                        g.setHoraEmision(rs.getTime("hora_emision"));
                        g.setEstadoGuia(rs.getString("estado_guia"));
                        lista.add(g);
                    }
                    return lista;
                }
                // Si la función no tiene las columnas necesarias, caerá al fallback abajo
            } catch (SQLException exFn) {
                // ignorar y usar fallback
            }

            // Fallback: intentar SELECT con columna eliminado
            try (PreparedStatement ps2 = cn.prepareStatement(sqlWithEliminado);
                 ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    CabeceraGuia g = new CabeceraGuia();
                    g.setCodigoGuia(rs2.getString("codigo_guia"));
                    g.setSerie(rs2.getString("serie"));
                    g.setNumero(rs2.getString("numero"));
                    g.setCodOrden(rs2.getString("cod_orden"));
                    g.setRucRemitente(rs2.getString("ruc_remitente"));
                    g.setRucDestinatario(rs2.getString("ruc_destinatario"));
                    g.setDirPartida(rs2.getString("dir_partida"));
                    g.setDirLlegada(rs2.getString("dir_llegada"));
                    g.setUbigeoOrigen(rs2.getString("ubigeo_origen"));
                    g.setUbigeoDestino(rs2.getString("ubigeo_destino"));
                    g.setPesoTotal(rs2.getObject("peso_total") == null ? 0.0 : rs2.getDouble("peso_total"));
                    g.setNumeroBultos(rs2.getObject("numero_bultos") == null ? 0 : rs2.getInt("numero_bultos"));
                    g.setFechaEmision(rs2.getDate("fecha_emision"));
                    g.setHoraEmision(rs2.getTime("hora_emision"));
                    g.setEstadoGuia(rs2.getString("estado_guia"));
                    lista.add(g);
                }
            } catch (SQLException ex2) {
                // último recurso: intentar fallback sin filtro eliminado
                try (PreparedStatement ps3 = cn.prepareStatement(sqlFallback);
                     ResultSet rs3 = ps3.executeQuery()) {
                    while (rs3.next()) {
                        CabeceraGuia g = new CabeceraGuia();
                        g.setCodigoGuia(rs3.getString("codigo_guia"));
                        g.setSerie(rs3.getString("serie"));
                        g.setNumero(rs3.getString("numero"));
                        g.setCodOrden(rs3.getString("cod_orden"));
                        g.setRucRemitente(rs3.getString("ruc_remitente"));
                        g.setRucDestinatario(rs3.getString("ruc_destinatario"));
                        g.setDirPartida(rs3.getString("dir_partida"));
                        g.setDirLlegada(rs3.getString("dir_llegada"));
                        g.setUbigeoOrigen(rs3.getString("ubigeo_origen"));
                        g.setUbigeoDestino(rs3.getString("ubigeo_destino"));
                        g.setPesoTotal(rs3.getObject("peso_total") == null ? 0.0 : rs3.getDouble("peso_total"));
                        g.setNumeroBultos(rs3.getObject("numero_bultos") == null ? 0 : rs3.getInt("numero_bultos"));
                        g.setFechaEmision(rs3.getDate("fecha_emision"));
                        g.setHoraEmision(rs3.getTime("hora_emision"));
                        g.setEstadoGuia(rs3.getString("estado_guia"));
                        lista.add(g);
                    }
                } catch (SQLException ex3) {
                    System.out.println("Error al listar guias (fallback final): " + ex3.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener conexión para listar guias: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarEstadoGuia(String codigoGuia, String nuevoEstado) {
        String call = "CALL sp_actualizar_estado_guia(?, ?)";
        String sql = "UPDATE cabecera_guia SET estado_guia = ? WHERE codigo_guia = ?";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigoGuia);
            cs.setString(2, nuevoEstado);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_actualizar_estado_guia failed: " + e.getMessage());
            // fallback to direct UPDATE
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setString(2, codigoGuia);
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback UPDATE estado_guia failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    /**
     * Emite la guía: intenta llamar a la función sp_emitir_guia (SELECT),
     * si no existe intenta CALL (procedimiento) y si sigue fallando hace un INSERT mínimo
     * directo en cabecera_guia como fallback.
     */
    public boolean emitirGuia(String codigoGuia,
                              String serie,
                              String numero,
                              String codOrden,
                              String rucRemitente,
                              String rucDestinatario,
                              String dirPartida,
                              String dirLlegada,
                              String ubigeoOrigen,
                              String ubigeoDestino,
                              String motivo,
                              String modalidad,
                              double pesoTotal,
                              int numeroBultos) {

        // 1) Intento función (SELECT)
        String fnSql = "SELECT sp_emitir_guia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(fnSql)) {

            ps.setString(1, codigoGuia);
            ps.setString(2, serie);
            ps.setString(3, numero);
            if (codOrden == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, codOrden);
            ps.setString(5, rucRemitente);
            ps.setString(6, rucDestinatario);
            ps.setString(7, dirPartida);
            ps.setString(8, dirLlegada);
            ps.setString(9, ubigeoOrigen);
            ps.setString(10, ubigeoDestino);
            ps.setString(11, motivo);
            ps.setString(12, modalidad);
            ps.setObject(13, pesoTotal);
            ps.setObject(14, numeroBultos);

            ps.execute();
            return true;
        } catch (SQLException ex1) {
            String msg = ex1.getMessage() == null ? "" : ex1.getMessage().toLowerCase();
            // Si función no existe, intento CALL (procedimiento)
            if (msg.contains("no existe la función") || msg.contains("no existe el procedimiento") || msg.contains("does not exist") || msg.contains("no existe")) {
                // 2) Intento CALL (procedure)
                String callSql = "{ call sp_emitir_guia(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
                try (Connection cn2 = Conexion.getConnection();
                     CallableStatement cs = cn2.prepareCall(callSql)) {

                    cs.setString(1, codigoGuia);
                    cs.setString(2, serie);
                    cs.setString(3, numero);
                    if (codOrden == null) cs.setNull(4, Types.VARCHAR); else cs.setString(4, codOrden);
                    cs.setString(5, rucRemitente);
                    cs.setString(6, rucDestinatario);
                    cs.setString(7, dirPartida);
                    cs.setString(8, dirLlegada);
                    cs.setString(9, ubigeoOrigen);
                    cs.setString(10, ubigeoDestino);
                    cs.setString(11, motivo);
                    cs.setString(12, modalidad);
                    cs.setObject(13, pesoTotal);
                    cs.setObject(14, numeroBultos);

                    cs.execute();
                    return true;
                } catch (SQLException ex2) {
                    // 3) Fallback: insertar mínimo en cabecera_guia (campos básicos y campos de traslado)
                    try (Connection cn3 = Conexion.getConnection()) {
                        // Asegurar que tenemos un ruc_remitente válido que exista en la tabla remitente.
                        String remitenteUse = (rucRemitente == null ? "" : rucRemitente.trim());
                        if (remitenteUse.isEmpty()) {
                            // intentar obtener cualquier remitente existente
                            String sel = "SELECT ruc FROM remitente LIMIT 1";
                            try (PreparedStatement psSel = cn3.prepareStatement(sel);
                                 ResultSet rsSel = psSel.executeQuery()) {
                                if (rsSel.next()) remitenteUse = rsSel.getString("ruc");
                            } catch (SQLException selEx) {
                                // ignorar
                            }
                        }

                        if (remitenteUse == null || remitenteUse.isEmpty()) {
                            // Insertar un remitente por defecto (si ya existe, ON CONFLICT no fallará)
                            remitenteUse = "00000000000";
                            String insRem = "INSERT INTO remitente (ruc, nombre) VALUES (?, ?) ON CONFLICT (ruc) DO NOTHING";
                            try (PreparedStatement psInsRem = cn3.prepareStatement(insRem)) {
                                psInsRem.setString(1, remitenteUse);
                                psInsRem.setString(2, "Remitente por defecto");
                                psInsRem.executeUpdate();
                            } catch (SQLException insRemEx) {
                                // reintentar obtener uno existente
                                try (PreparedStatement psSel2 = cn3.prepareStatement("SELECT ruc FROM remitente LIMIT 1");
                                     ResultSet rs2 = psSel2.executeQuery()) {
                                    if (rs2.next()) remitenteUse = rs2.getString("ruc");
                                } catch (SQLException ignore) { /* continuará y puede fallar más abajo */ }
                            }
                        }

                        // Ahora insertamos la cabecera usando remitenteUse (garantizar FK)
                        // Incluimos ruc_destinatario, direcciones, ubigeos, peso_total y numero_bultos
                        String ins = "INSERT INTO cabecera_guia (" +
                                     "codigo_guia, serie, numero, cod_orden, ruc_remitente, ruc_destinatario, " +
                                     "dir_partida, dir_llegada, ubigeo_origen, ubigeo_destino, peso_total, numero_bultos, " +
                                     "fecha_emision, hora_emision, estado_guia" +
                                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                     "ON CONFLICT (codigo_guia) DO UPDATE SET " +
                                     "serie = EXCLUDED.serie, numero = EXCLUDED.numero, cod_orden = EXCLUDED.cod_orden, " +
                                     "ruc_remitente = EXCLUDED.ruc_remitente, ruc_destinatario = EXCLUDED.ruc_destinatario, " +
                                     "dir_partida = EXCLUDED.dir_partida, dir_llegada = EXCLUDED.dir_llegada, " +
                                     "ubigeo_origen = EXCLUDED.ubigeo_origen, ubigeo_destino = EXCLUDED.ubigeo_destino, " +
                                     "peso_total = EXCLUDED.peso_total, numero_bultos = EXCLUDED.numero_bultos, " +
                                     "fecha_emision = EXCLUDED.fecha_emision, hora_emision = EXCLUDED.hora_emision, " +
                                     "estado_guia = EXCLUDED.estado_guia";
                        try (PreparedStatement psIns = cn3.prepareStatement(ins)) {
                            psIns.setString(1, codigoGuia);
                            psIns.setString(2, serie);
                            psIns.setString(3, numero);
                            if (codOrden == null) psIns.setNull(4, Types.VARCHAR); else psIns.setString(4, codOrden);
                            psIns.setString(5, remitenteUse);
                            psIns.setString(6, (rucDestinatario == null ? "" : rucDestinatario));
                            psIns.setString(7, dirPartida == null ? "" : dirPartida);
                            psIns.setString(8, dirLlegada == null ? "" : dirLlegada);
                            psIns.setString(9, ubigeoOrigen == null ? "" : ubigeoOrigen);
                            psIns.setString(10, ubigeoDestino == null ? "" : ubigeoDestino);
                            psIns.setObject(11, pesoTotal);
                            psIns.setObject(12, numeroBultos);
                            psIns.setDate(13, java.sql.Date.valueOf(LocalDate.now()));
                            psIns.setTime(14, java.sql.Time.valueOf(LocalTime.now()));
                            psIns.setString(15, "emitida");
                            psIns.executeUpdate();
                            return true;
                        }
                    } catch (SQLException ex3) {
                        System.out.println("Error fallback emitirGuia (insert): " + ex3.getMessage());
                        return false;
                    }
                }
            } else {
                System.out.println("Error al emitir guía (SELECT): " + ex1.getMessage());
                return false;
            }
        }
    }

    /**
     * Soft-delete: marcar eliminado = true (requiere columna eliminado boolean en la tabla)
     */
    public boolean eliminar(String codigoGuia) {
        String call = "CALL sp_eliminar_guia(?)";
        String sql = "UPDATE cabecera_guia SET eliminado = true WHERE codigo_guia = ?";
        try (Connection cn = Conexion.getConnection(); CallableStatement cs = cn.prepareCall(call)) {
            cs.setString(1, codigoGuia);
            cs.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("sp_eliminar_guia failed: " + e.getMessage());
            // fallback to soft-delete
            try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps = cn2.prepareStatement(sql)) {
                ps.setString(1, codigoGuia);
                return ps.executeUpdate() > 0;
            } catch (SQLException ex2) {
                System.err.println("Fallback soft-delete guia failed: " + ex2.getMessage());
                return false;
            }
        }
    }

    // Recuperar registro eliminado (soft-undelete)
    public boolean recuperar(String codigoGuia) {
        String sql = "UPDATE cabecera_guia SET eliminado = false WHERE codigo_guia = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigoGuia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al recuperar guía: " + e.getMessage());
            return false;
        }
    }

    public List<DetalleGuia> listarDetallePorGuia(String codigoGuia) {
        List<DetalleGuia> lista = new ArrayList<>();
        String sql = "SELECT nro_item AS nro_item, bien_normalizado, codigo_bien, codigo_producto_sunat, partida_arancelaria, " +
                     "codigo_gtin, descripcion, unidad_medida, cantidad " +
                     "FROM detalle_guia WHERE codigo_guia = ? ORDER BY nro_item";

        try (Connection cn = Conexion.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigoGuia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nro = rs.getInt("nro_item");
                    String bien = rs.getString("bien_normalizado");
                    String codBien = rs.getString("codigo_bien");
                    String codProd = rs.getString("codigo_producto_sunat");
                    String partida = rs.getString("partida_arancelaria");
                    String gtin = rs.getString("codigo_gtin");
                    String desc = rs.getString("descripcion");
                    String unidad = rs.getString("unidad_medida");
                    String cantidad = rs.getString("cantidad");
                    DetalleGuia d = new DetalleGuia(nro, bien, codBien, codProd, partida, gtin, desc, unidad, cantidad);
                    lista.add(d);
                }
            }

        } catch (SQLException e) {
            // Si la tabla detalle_guia no existe, intentar reconstruir desde detalle_orden (usada por órdenes)
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("detalle_guia") || msg.contains("no existe la relación") || "42p01".equals(e.getSQLState())) {
                try {
                    // Obtener la orden asociada a esta guía
                    CabeceraGuia cab = obtenerPorCodigo(codigoGuia);
                    if (cab != null && cab.getCodOrden() != null && !cab.getCodOrden().trim().isEmpty()) {
                        String codigoOrden = cab.getCodOrden();
                        String sql2 = "SELECT numero_item, codigo_producto, cantidad FROM detalle_orden WHERE codigo_orden = ? ORDER BY numero_item";
                        try (Connection cn2 = Conexion.getConnection(); PreparedStatement ps2 = cn2.prepareStatement(sql2)) {
                            ps2.setString(1, codigoOrden);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                dao.ProductoDAO prodDao = new dao.ProductoDAO();
                                while (rs2.next()) {
                                    int nro = rs2.getInt("numero_item");
                                    String codigoProd = rs2.getString("codigo_producto");
                                    String cantidad = null;
                                    try {
                                        java.math.BigDecimal bd = rs2.getBigDecimal("cantidad");
                                        if (bd != null) cantidad = bd.stripTrailingZeros().toPlainString();
                                    } catch (SQLException ex) {
                                        cantidad = rs2.getString("cantidad");
                                    }

                                    String nombre = codigoProd;
                                    String unidad = "";
                                    try {
                                        model.Producto p = prodDao.buscarPorCodigo(codigoProd);
                                        if (p != null) {
                                            if (p.getNombreProducto() != null) nombre = p.getNombreProducto();
                                            if (p.getUnidadMedida() != null) unidad = p.getUnidadMedida();
                                        }
                                    } catch (Exception ignore) { }

                                    // Mapear campos disponibles de detalle_orden/producto a DetalleGuia
                                    DetalleGuia d = new DetalleGuia(
                                            nro,
                                            nombre,            // bien_normalizado: usar nombre del producto cuando esté disponible
                                            "",                // codigo_bien: no disponible
                                            codigoProd,         // codigo_producto_sunat
                                            "",                // partida_arancelaria: no disponible
                                            "",                // codigo_gtin: no disponible
                                            nombre,             // descripcion
                                            unidad,             // unidad_medida
                                            (cantidad == null ? "" : cantidad) // cantidad
                                    );
                                    lista.add(d);
                                }
                            }
                        } catch (SQLException ex2) {
                            System.out.println("Error al reconstruir detalle de guía desde detalle_orden: " + ex2.getMessage());
                        }
                    } else {
                        System.out.println("No se puede reconstruir detalle de guía: guía sin orden asociada");
                    }
                } catch (Exception ex) {
                    System.out.println("Error al intentar fallback detalle_guia -> detalle_orden: " + ex.getMessage());
                }
            } else {
                System.out.println("Error al listar detalle de guía: " + e.getMessage());
            }
        }

        return lista;
    }

    /**
     * Obtiene la cabecera completa por código (trae todos los campos usados en el formulario).
     */
    public CabeceraGuia obtenerPorCodigo(String codigoGuia) {
        String sql = "SELECT codigo_guia, serie, numero, cod_orden, ruc_remitente, ruc_destinatario, " +
                "dir_partida, dir_llegada, ubigeo_origen, ubigeo_destino, peso_total, numero_bultos, " +
                "fecha_emision, hora_emision, estado_guia " +
                "FROM cabecera_guia WHERE codigo_guia = ? LIMIT 1";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigoGuia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CabeceraGuia g = new CabeceraGuia();
                    g.setCodigoGuia(rs.getString("codigo_guia"));
                    g.setSerie(rs.getString("serie"));
                    g.setNumero(rs.getString("numero"));
                    g.setCodOrden(rs.getString("cod_orden"));
                    g.setRucRemitente(rs.getString("ruc_remitente"));
                    g.setRucDestinatario(rs.getString("ruc_destinatario"));
                    g.setDirPartida(rs.getString("dir_partida"));
                    g.setDirLlegada(rs.getString("dir_llegada"));
                    g.setUbigeoOrigen(rs.getString("ubigeo_origen"));
                    g.setUbigeoDestino(rs.getString("ubigeo_destino"));
                    g.setPesoTotal(rs.getObject("peso_total") == null ? 0.0 : rs.getDouble("peso_total"));
                    g.setNumeroBultos(rs.getObject("numero_bultos") == null ? 0 : rs.getInt("numero_bultos"));
                    g.setFechaEmision(rs.getDate("fecha_emision"));
                    g.setHoraEmision(rs.getTime("hora_emision"));
                    g.setEstadoGuia(rs.getString("estado_guia"));
                    return g;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerPorCodigo: " + e.getMessage());
        }
        return null;
    }
}
