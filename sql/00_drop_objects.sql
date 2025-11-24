-- Safe drop script to allow re-run of the bootstrap without manual cleanup.
-- Drops functions, procedures, triggers and tables used by the schema (idempotent).

-- Drop triggers (if exist)
DROP TRIGGER IF EXISTS trg_before_delete_destinatario ON destinatario;
DROP TRIGGER IF EXISTS trg_before_delete_remitente ON remitente;
DROP TRIGGER IF EXISTS trg_before_delete_producto ON producto;
DROP TRIGGER IF EXISTS trg_before_update_estado_traslado ON traslado;
DROP TRIGGER IF EXISTS trg_detalle_orden_bi ON detalle_orden;
DROP TRIGGER IF EXISTS trg_detalle_orden_bu ON detalle_orden;
DROP TRIGGER IF EXISTS trg_detalle_bienes_bi ON detalle_bienes;
DROP TRIGGER IF EXISTS trg_traslado_ai ON traslado;
DROP TRIGGER IF EXISTS trg_traslado_au ON traslado;

-- Drop trigger functions
DROP FUNCTION IF EXISTS f_trg_before_delete_destinatario();
DROP FUNCTION IF EXISTS f_trg_before_delete_remitente();
DROP FUNCTION IF EXISTS f_trg_before_delete_producto();
DROP FUNCTION IF EXISTS f_trg_before_update_estado_traslado();
DROP FUNCTION IF EXISTS f_trg_detalle_orden_bi();
DROP FUNCTION IF EXISTS f_trg_detalle_orden_bu();
DROP FUNCTION IF EXISTS f_trg_detalle_bienes_bi();
DROP FUNCTION IF EXISTS f_trg_traslado_sync_estado();

-- Drop procedures and functions (list is not exhaustive but covers the schema)
-- Procedures
DROP PROCEDURE IF EXISTS sp_insertar_destinatario;
DROP PROCEDURE IF EXISTS sp_actualizar_destinatario;
DROP PROCEDURE IF EXISTS sp_eliminar_destinatario;
DROP PROCEDURE IF EXISTS sp_insertar_remitente;
DROP PROCEDURE IF EXISTS sp_actualizar_remitente;
DROP PROCEDURE IF EXISTS sp_eliminar_remitente;
DROP PROCEDURE IF EXISTS sp_insertar_producto;
DROP PROCEDURE IF EXISTS sp_actualizar_producto;
DROP PROCEDURE IF EXISTS sp_eliminar_producto;
DROP PROCEDURE IF EXISTS sp_insertar_vehiculo;
DROP PROCEDURE IF EXISTS sp_actualizar_vehiculo;
DROP PROCEDURE IF EXISTS sp_eliminar_vehiculo;
DROP PROCEDURE IF EXISTS sp_insertar_conductor;
DROP PROCEDURE IF EXISTS sp_actualizar_conductor;
DROP PROCEDURE IF EXISTS sp_eliminar_conductor;
DROP PROCEDURE IF EXISTS sp_crear_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_actualizar_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_eliminar_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_agregar_detalle_orden;
DROP PROCEDURE IF EXISTS sp_insertar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_actualizar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_eliminar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_agregar_detalle_bien_guia;
DROP PROCEDURE IF EXISTS sp_emitir_guia;
DROP PROCEDURE IF EXISTS sp_generar_guia;
DROP PROCEDURE IF EXISTS sp_registrar_traslado;
DROP PROCEDURE IF EXISTS sp_actualizar_traslado;
DROP PROCEDURE IF EXISTS sp_eliminar_traslado;
DROP PROCEDURE IF EXISTS sp_actualizar_estado_traslado;
DROP PROCEDURE IF EXISTS sp_confirmar_entrega;
DROP PROCEDURE IF EXISTS sp_eliminar_detalle_por_orden(VARCHAR);

-- Functions (reports and lists)
DROP FUNCTION IF EXISTS sp_listar_destinatarios();
DROP FUNCTION IF EXISTS sp_listar_remitentes();
DROP FUNCTION IF EXISTS sp_buscar_remitente(TEXT);
DROP FUNCTION IF EXISTS sp_listar_productos();
DROP FUNCTION IF EXISTS sp_listar_ubigeos();
DROP FUNCTION IF EXISTS sp_listar_vehiculos();
DROP FUNCTION IF EXISTS sp_listar_conductores();
DROP FUNCTION IF EXISTS sp_listar_ordenes();
DROP FUNCTION IF EXISTS sp_listar_guias();
DROP FUNCTION IF EXISTS sp_listar_traslados();
DROP FUNCTION IF EXISTS sp_reporte_detalle_orden(VARCHAR);
DROP FUNCTION IF EXISTS sp_reporte_guias_por_fecha_estado();
DROP FUNCTION IF EXISTS sp_reporte_productos_mas_vendidos();
DROP FUNCTION IF EXISTS sp_reporte_utilizacion_vehiculos();
DROP FUNCTION IF EXISTS sp_reporte_licencias_por_vencer(INT);
DROP FUNCTION IF EXISTS sp_reporte_guias_sin_traslado();
DROP FUNCTION IF EXISTS sp_reporte_ordenes_por_cliente();
DROP FUNCTION IF EXISTS sp_reporte_bultos_por_cliente_90d();
DROP FUNCTION IF EXISTS sp_reporte_kpi_guias_diario();
DROP FUNCTION IF EXISTS sp_reporte_ingresos_por_fecha();
DROP FUNCTION IF EXISTS sp_reporte_historial_estados_traslado();

-- Drop audit tables (if any dependencies remain, CASCADE will remove them)
DROP TABLE IF EXISTS traslado_log_estado CASCADE;
DROP TABLE IF EXISTS producto_eliminado CASCADE;
DROP TABLE IF EXISTS remitente_eliminado CASCADE;
DROP TABLE IF EXISTS destinatario_eliminado CASCADE;

-- Drop data/tables (drop in order to avoid FK issues)
DROP TABLE IF EXISTS detalle_bienes CASCADE;
DROP TABLE IF EXISTS bien_transportable CASCADE;
DROP TABLE IF EXISTS cuerpo_guia CASCADE;
DROP TABLE IF EXISTS cabecera_guia CASCADE;
DROP TABLE IF EXISTS traslado CASCADE;
DROP TABLE IF EXISTS detalle_orden CASCADE;
DROP TABLE IF EXISTS orden_de_pago CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS destinatario CASCADE;
DROP TABLE IF EXISTS remitente CASCADE;
DROP TABLE IF EXISTS vehiculo CASCADE;
DROP TABLE IF EXISTS conductor CASCADE;
DROP TABLE IF EXISTS ubigeo CASCADE;

-- End of drop script
