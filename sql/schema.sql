-- =========================================================
-- User-provided complete schema (original content)
-- Paste of user's script saved as-is. Edit or split into parts as needed.
-- =========================================================

-- =========================================================
-- SCRIPT COMPLETO PARA PostgreSQL / pgAdmin
-- Basado en tu script MySQL original (tablas, datos, SP, triggers, reportes)
-- La base de datos guia_remision debe estar creada y seleccionada en pgAdmin.
-- =========================================================


-- =========================================================
-- 1. TABLAS BASE
-- =========================================================

-- UBIGEO
CREATE TABLE IF NOT EXISTS ubigeo (
  codigo_ubigeo VARCHAR(6) PRIMARY KEY,
  departamento  VARCHAR(50) NOT NULL,
  provincia     VARCHAR(50) NOT NULL,
  distrito      VARCHAR(50) NOT NULL
);

-- REMITENTE
CREATE TABLE IF NOT EXISTS remitente (
  ruc CHAR(11) PRIMARY KEY,
  nombre_empresa VARCHAR(120) NOT NULL,
  razon_social   VARCHAR(120) NOT NULL,
  telefono       VARCHAR(20),
  email          VARCHAR(120),
  calle_direccion VARCHAR(120) NOT NULL,
  codigo_ubigeo  VARCHAR(6) NOT NULL REFERENCES ubigeo(codigo_ubigeo)
);

-- DESTINATARIO
CREATE TABLE IF NOT EXISTS destinatario (
  ruc CHAR(11) PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  numero_telefono VARCHAR(20),
  calle_direccion VARCHAR(120) NOT NULL,
  codigo_ubigeo   VARCHAR(6) NOT NULL REFERENCES ubigeo(codigo_ubigeo),
  gmail           VARCHAR(120)
);

-- ORDEN_DE_PAGO
CREATE TABLE IF NOT EXISTS orden_de_pago (
  codigo_orden VARCHAR(15) PRIMARY KEY,
  fecha DATE NOT NULL,
  ruc_cliente CHAR(11) NOT NULL REFERENCES destinatario(ruc),
  estado VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orden_fecha   ON orden_de_pago(fecha);
CREATE INDEX IF NOT EXISTS idx_orden_cliente ON orden_de_pago(ruc_cliente);

-- PRODUCTO
CREATE TABLE IF NOT EXISTS producto (
  codigo_producto  VARCHAR(20) PRIMARY KEY,
  nombre_producto  VARCHAR(120) NOT NULL,
  precio_base      NUMERIC(10,2) CHECK (precio_base >= 0),
  unidad_medida    VARCHAR(20) NOT NULL
);

-- DETALLE_ORDEN
CREATE TABLE IF NOT EXISTS detalle_orden (
  numero_item      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_orden     VARCHAR(15) NOT NULL REFERENCES orden_de_pago(codigo_orden),
  codigo_producto  VARCHAR(20) NOT NULL REFERENCES producto(codigo_producto),
  cantidad         NUMERIC(10,2) CHECK (cantidad >= 0),
  precio_unitario  NUMERIC(10,2) CHECK (precio_unitario >= 0),
  subtotal         NUMERIC(10,2) CHECK (subtotal >= 0)
);

CREATE INDEX IF NOT EXISTS idx_det_orden
  ON detalle_orden(codigo_orden, codigo_producto);

-- CABECERA_GUIA
CREATE TABLE IF NOT EXISTS cabecera_guia (
  codigo_guia   VARCHAR(15) PRIMARY KEY,
  serie         VARCHAR(10) NOT NULL,
  numero        VARCHAR(10) NOT NULL,
  cod_orden     VARCHAR(15) UNIQUE REFERENCES orden_de_pago(codigo_orden),
  ruc_remitente CHAR(11) NOT NULL REFERENCES remitente(ruc),
  fecha_emision DATE NOT NULL,
  hora_emision  TIME,
  estado_guia   VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cabecera_fecha
  ON cabecera_guia(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_cabecera_estado
  ON cabecera_guia(estado_guia);

-- CUERPO_GUIA
CREATE TABLE IF NOT EXISTS cuerpo_guia (
  codigo_guia        VARCHAR(15) PRIMARY KEY
                     REFERENCES cabecera_guia(codigo_guia),
  ruc_destinatario   CHAR(11) NOT NULL REFERENCES destinatario(ruc),
  direccion_partida  VARCHAR(120) NOT NULL,
  direccion_llegada  VARCHAR(120) NOT NULL,
  ubigeo_origen      VARCHAR(6) NOT NULL REFERENCES ubigeo(codigo_ubigeo),
  ubigeo_destino     VARCHAR(6) NOT NULL REFERENCES ubigeo(codigo_ubigeo),
  motivo_traslado    VARCHAR(100) NOT NULL,
  modalidad_transporte VARCHAR(50) NOT NULL,
  peso_total         NUMERIC(10,2) CHECK (peso_total >= 0),
  numero_bultos      INTEGER CHECK (numero_bultos >= 0)
);

CREATE INDEX IF NOT EXISTS idx_cuerpo_destinatario
  ON cuerpo_guia(ruc_destinatario);
CREATE INDEX IF NOT EXISTS idx_cuerpo_origen_destino
  ON cuerpo_guia(ubigeo_origen, ubigeo_destino);

-- BIEN_TRANSPORTABLE
CREATE TABLE IF NOT EXISTS bien_transportable (
  codigo_bien VARCHAR(20) PRIMARY KEY,
  descripcion VARCHAR(120) NOT NULL,
  unidad_medida VARCHAR(20) NOT NULL,
  peso_unitario_promedio NUMERIC(10,2) CHECK (peso_unitario_promedio >= 0)
);

-- DETALLE_BIENES
CREATE TABLE IF NOT EXISTS detalle_bienes (
  numero_item  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_guia  VARCHAR(15) NOT NULL REFERENCES cabecera_guia(codigo_guia),
  codigo_bien  VARCHAR(20) NOT NULL REFERENCES bien_transportable(codigo_bien),
  cantidad     NUMERIC(10,2) CHECK (cantidad >= 0),
  peso_total   NUMERIC(10,2) CHECK (peso_total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_det_bienes
  ON detalle_bienes(codigo_guia, codigo_bien);

-- VEHICULO
CREATE TABLE IF NOT EXISTS vehiculo (
  placa VARCHAR(10) PRIMARY KEY,
  numero_mtc VARCHAR(20) UNIQUE,
  tipo_vehiculo VARCHAR(50) NOT NULL,
  marca VARCHAR(50),
  modelo VARCHAR(50),
  carga_max NUMERIC(10,2) CHECK (carga_max >= 0)
);

-- CONDUCTOR
CREATE TABLE IF NOT EXISTS conductor (
  licencia VARCHAR(15) PRIMARY KEY,
  dni CHAR(8) UNIQUE,
  nombre VARCHAR(120) NOT NULL,
  telefono VARCHAR(20),
  fecha_vencimiento_licencia DATE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_conductor_vigencia
  ON conductor(fecha_vencimiento_licencia);

-- TRASLADO
CREATE TABLE IF NOT EXISTS traslado (
  codigo_traslado VARCHAR(15) PRIMARY KEY,
  codigo_guia     VARCHAR(15) UNIQUE REFERENCES cabecera_guia(codigo_guia),
  placa           VARCHAR(10) NOT NULL REFERENCES vehiculo(placa),
  licencia        VARCHAR(15) NOT NULL REFERENCES conductor(licencia),
  fecha_inicio    TIMESTAMP NOT NULL,
  fecha_fin       TIMESTAMP NOT NULL,
  estado_traslado VARCHAR(20) NOT NULL,
  observaciones   TEXT,
  CHECK (fecha_fin >= fecha_inicio)
);

CREATE INDEX IF NOT EXISTS idx_traslado_estado
  ON traslado(estado_traslado);
CREATE INDEX IF NOT EXISTS idx_traslado_inicio
  ON traslado(fecha_inicio);
CREATE INDEX IF NOT EXISTS idx_traslado_placa
  ON traslado(placa);


-- =========================================================
-- 2. DATOS DE EJEMPLO (INSERTS)
-- =========================================================

-- UBIGEO
INSERT INTO ubigeo VALUES
('040101', 'Arequipa', 'Arequipa', 'Cercado'),
('040102', 'Arequipa', 'Arequipa', 'Cayma'),
('040103', 'Arequipa', 'Arequipa', 'Cerro Colorado'),
('150101', 'Lima', 'Lima', 'Miraflores'),
('150102', 'Lima', 'Lima', 'San Isidro');

-- REMITENTE
INSERT INTO remitente VALUES
('20604567891', 'LIPA S.A.C.', 'Logística Integral del Perú S.A.C.',
 '054212345', 'contacto@lipa.com.pe',
 'Av. Ejército 1500 - Cayma', '040102');

-- DESTINATARIO
INSERT INTO destinatario VALUES
('20123456789', 'FERRETERÍA EL MISTI S.A.C.', '054345678',
 'Av. Mariscal Castilla 450', '040103', 'ventas@mistiferreteria.pe'),
('20112233445', 'CONSTRUCTORA SUR E.I.R.L.', '054223344',
 'Calle Paucarpata 305', '040101', 'compras@surconstructora.pe'),
('20559887766', 'ALMACÉN CENTRAL PERÚ S.A.', '014558877',
 'Av. Javier Prado 1234', '150101', 'almacen@centralperu.pe');

-- PRODUCTO
INSERT INTO producto VALUES
('PRD001', 'Tubo PVC 1/2 pulgada', 12.50, 'unidad'),
('PRD002', 'Cemento Portland 50kg', 32.00, 'bolsa'),
('PRD003', 'Arena fina (m3)', 45.00, 'm3'),
('PRD004', 'Clavo de acero 2"', 9.80, 'kg');

-- ORDEN_DE_PAGO
INSERT INTO orden_de_pago VALUES
('ORD0001', '2025-10-20', '20123456789', 'emitida'),
('ORD0002', '2025-10-19', '20112233445', 'emitida');

-- DETALLE_ORDEN
INSERT INTO detalle_orden (codigo_orden, codigo_producto, cantidad, precio_unitario, subtotal)
VALUES
('ORD0001', 'PRD001', 10, 12.50, 125.00),
('ORD0001', 'PRD002', 5, 32.00, 160.00),
('ORD0002', 'PRD003', 3, 45.00, 135.00),
('ORD0002', 'PRD004', 2, 9.80, 19.60);

-- CABECERA_GUIA
INSERT INTO cabecera_guia VALUES
('GUIA0001', 'A001', '000001', 'ORD0001', '20604567891',
 '2025-10-21', '09:30:00', 'emitida'),
('GUIA0002', 'A001', '000002', 'ORD0002', '20604567891',
 '2025-10-21', '10:15:00', 'emitida');

-- CUERPO_GUIA
INSERT INTO cuerpo_guia VALUES
('GUIA0001', '20123456789',
 'Av. Ejército 1500 - Cayma',
 'Av. Mariscal Castilla 450 - Cerro Colorado',
 '040102', '040103', 'Entrega de materiales',
 'terrestre', 285.00, 15),
('GUIA0002', '20112233445',
 'Av. Ejército 1500 - Cayma',
 'Calle Paucarpata 305 - Cercado',
 '040102', '040101', 'Suministro de obra',
 'terrestre', 200.00, 8);

-- BIEN_TRANSPORTABLE
INSERT INTO bien_transportable VALUES
('B001', 'Cemento Portland 50kg', 'bolsa', 50.00),
('B002', 'Tubo PVC 1/2 pulgada', 'unidad', 0.25),
('B003', 'Arena fina (m3)', 'm3', 1200.00),
('B004', 'Clavo de acero 2"', 'kg', 1.00);

-- DETALLE_BIENES
INSERT INTO detalle_bienes (codigo_guia, codigo_bien, cantidad, peso_total)
VALUES
('GUIA0001', 'B001', 5, 250.00),
('GUIA0001', 'B002', 10, 2.50),
('GUIA0002', 'B003', 3, 3600.00),
('GUIA0002', 'B004', 2, 2.00);

-- VEHICULO
INSERT INTO vehiculo VALUES
('V1A-456', 'MTC-998877', 'Camión mediano', 'Volvo', 'FL6', 8500.00),
('V9Z-325', 'MTC-884422', 'Camioneta', 'Toyota', 'Hilux', 1200.00);

-- CONDUCTOR
INSERT INTO conductor VALUES
('LIC123456789', '70451236', 'Carlos Gutiérrez Ramos',
 '958745632', '2026-05-10'),
('LIC987654321', '71325489', 'José Luis Medina Paredes',
 '976554321', '2027-02-15');

-- TRASLADO
INSERT INTO traslado VALUES
('TRS0001', 'GUIA0001', 'V1A-456', 'LIC123456789',
 TIMESTAMP '2025-10-21 10:00:00',
 TIMESTAMP '2025-10-21 16:00:00',
 'en tránsito',
 'Entrega directa al cliente en Cerro Colorado'),
('TRS0002', 'GUIA0002', 'V9Z-325', 'LIC987654321',
 TIMESTAMP '2025-10-21 11:00:00',
 TIMESTAMP '2025-10-21 15:30:00',
 'entregado',
 'Descarga completa en obra de Cercado');


-- =========================================================
-- 3. TABLAS DE AUDITORÍA / LOG
-- =========================================================

CREATE TABLE IF NOT EXISTS destinatario_eliminado (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  ruc CHAR(11),
  nombre VARCHAR(120),
  numero_telefono VARCHAR(20),
  calle_direccion VARCHAR(120),
  codigo_ubigeo VARCHAR(6),
  gmail VARCHAR(120),
  fecha_eliminacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS remitente_eliminado (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  ruc CHAR(11),
  nombre_empresa VARCHAR(120),
  razon_social VARCHAR(120),
  telefono VARCHAR(20),
  email VARCHAR(120),
  calle_direccion VARCHAR(120),
  codigo_ubigeo VARCHAR(6),
  fecha_eliminacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS producto_eliminado (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_producto VARCHAR(20),
  nombre_producto VARCHAR(120),
  precio_base NUMERIC(10,2),
  unidad_medida VARCHAR(20),
  fecha_eliminacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS traslado_log_estado (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo_traslado VARCHAR(15),
  estado_anterior VARCHAR(20),
  estado_nuevo    VARCHAR(20),
  fecha_cambio    TIMESTAMP NOT NULL DEFAULT NOW()
);


-- =========================================================
-- 4. TRIGGERS (FUNCTIONS + TRIGGERS)
-- =========================================================

-- Reemplaza o crea la función con tipos que coincidan con la tabla conductor
DROP FUNCTION IF EXISTS sp_reporte_licencias_por_vencer(character varying);

-- 2) Recreate function with types that match your conductor table
CREATE OR REPLACE FUNCTION sp_reporte_licencias_por_vencer(p_dias varchar)
RETURNS TABLE(
    licencia varchar(15),
    dni char(8),
    nombre varchar(120),
    telefono varchar(20),
    fecha_vencimiento date,
    dias_restantes integer
)
LANGUAGE plpgsql
AS $$
DECLARE
    ndays integer;
BEGIN
    BEGIN
        ndays := NULLIF(trim(p_dias), '')::integer;
    EXCEPTION WHEN others THEN
        ndays := 90;
    END;
    IF ndays IS NULL THEN ndays := 90; END IF;

    RETURN QUERY
    SELECT
        c.licencia,
        c.dni,
        c.nombre,
        c.telefono,
        c.fecha_vencimiento_licencia AS fecha_vencimiento,
        (c.fecha_vencimiento_licencia - current_date) AS dias_restantes
    FROM conductor c
    WHERE c.fecha_vencimiento_licencia IS NOT NULL
      AND c.fecha_vencimiento_licencia
          BETWEEN current_date AND (current_date + (ndays * INTERVAL '1 day'))::date
    ORDER BY c.fecha_vencimiento_licencia;
END;
$$;

-- DESTINATARIO: log antes de borrar
CREATE OR REPLACE FUNCTION f_trg_before_delete_destinatario()
RETURNS trigger AS $$
BEGIN
  INSERT INTO destinatario_eliminado (
    ruc, nombre, numero_telefono, calle_direccion, codigo_ubigeo, gmail
  )
  VALUES (
    OLD.ruc, OLD.nombre, OLD.numero_telefono,
    OLD.calle_direccion, OLD.codigo_ubigeo, OLD.gmail
  );
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_before_delete_destinatario ON destinatario;
CREATE TRIGGER trg_before_delete_destinatario
BEFORE DELETE ON destinatario
FOR EACH ROW
EXECUTE FUNCTION f_trg_before_delete_destinatario();

-- REMITENTE: log antes de borrar
CREATE OR REPLACE FUNCTION f_trg_before_delete_remitente()
RETURNS trigger AS $$
BEGIN
  INSERT INTO remitente_eliminado (
    ruc, nombre_empresa, razon_social, telefono, email,
    calle_direccion, codigo_ubigeo
  )
  VALUES (
    OLD.ruc, OLD.nombre_empresa, OLD.razon_social,
    OLD.telefono, OLD.email, OLD.calle_direccion, OLD.codigo_ubigeo
  );
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_before_delete_remitente ON remitente;
CREATE TRIGGER trg_before_delete_remitente
BEFORE DELETE ON remitente
FOR EACH ROW
EXECUTE FUNCTION f_trg_before_delete_remitente();

-- PRODUCTO: log antes de borrar
CREATE OR REPLACE FUNCTION f_trg_before_delete_producto()
RETURNS trigger AS $$
BEGIN
  INSERT INTO producto_eliminado (
    codigo_producto, nombre_producto, precio_base, unidad_medida
  )
  VALUES (
    OLD.codigo_producto, OLD.nombre_producto,
    OLD.precio_base, OLD.unidad_medida
  );
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_before_delete_producto ON producto;
CREATE TRIGGER trg_before_delete_producto
BEFORE DELETE ON producto
FOR EACH ROW
EXECUTE FUNCTION f_trg_before_delete_producto();

-- TRASLADO: historial cambio de estado
CREATE OR REPLACE FUNCTION f_trg_before_update_estado_traslado()
RETURNS trigger AS $$
BEGIN
  IF COALESCE(OLD.estado_traslado,'') IS DISTINCT FROM COALESCE(NEW.estado_traslado,'') THEN
    INSERT INTO traslado_log_estado (
      codigo_traslado, estado_anterior, estado_nuevo
    )
    VALUES (
      OLD.codigo_traslado, OLD.estado_traslado, NEW.estado_traslado
    );
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_before_update_estado_traslado ON traslado;
CREATE TRIGGER trg_before_update_estado_traslado
BEFORE UPDATE ON traslado
FOR EACH ROW
EXECUTE FUNCTION f_trg_before_update_estado_traslado();

-- DETALLE_ORDEN: subtotal en insert
CREATE OR REPLACE FUNCTION f_trg_detalle_orden_bi()
RETURNS trigger AS $$
BEGIN
  IF NEW.subtotal IS NULL THEN
    NEW.subtotal := NEW.cantidad * NEW.precio_unitario;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_detalle_orden_bi ON detalle_orden;
CREATE TRIGGER trg_detalle_orden_bi
BEFORE INSERT ON detalle_orden
FOR EACH ROW
EXECUTE FUNCTION f_trg_detalle_orden_bi();

-- DETALLE_ORDEN: subtotal en update
CREATE OR REPLACE FUNCTION f_trg_detalle_orden_bu()
RETURNS trigger AS $$
BEGIN
  NEW.subtotal := NEW.cantidad * NEW.precio_unitario;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_detalle_orden_bu ON detalle_orden;
CREATE TRIGGER trg_detalle_orden_bu
BEFORE UPDATE ON detalle_orden
FOR EACH ROW
EXECUTE FUNCTION f_trg_detalle_orden_bu();

-- DETALLE_BIENES: peso_total si viene NULL
CREATE OR REPLACE FUNCTION f_trg_detalle_bienes_bi()
RETURNS trigger AS $$
BEGIN
  IF NEW.peso_total IS NULL THEN
    SELECT b.peso_unitario_promedio * NEW.cantidad
      INTO NEW.peso_total
      FROM bien_transportable b
     WHERE b.codigo_bien = NEW.codigo_bien;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_detalle_bienes_bi ON detalle_bienes;
CREATE TRIGGER trg_detalle_bienes_bi
BEFORE INSERT ON detalle_bienes
FOR EACH ROW
EXECUTE FUNCTION f_trg_detalle_bienes_bi();

-- TRASLADO: sincronizar estado_guia con estado_traslado (insert / update)
CREATE OR REPLACE FUNCTION f_trg_traslado_sync_estado()
RETURNS trigger AS $$
BEGIN
  UPDATE cabecera_guia
     SET estado_guia = NEW.estado_traslado
   WHERE codigo_guia = NEW.codigo_guia;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_traslado_ai ON traslado;
CREATE TRIGGER trg_traslado_ai
AFTER INSERT ON traslado
FOR EACH ROW
EXECUTE FUNCTION f_trg_traslado_sync_estado();

DROP TRIGGER IF EXISTS trg_traslado_au ON traslado;
CREATE TRIGGER trg_traslado_au
AFTER UPDATE ON traslado
FOR EACH ROW
EXECUTE FUNCTION f_trg_traslado_sync_estado();


-- =========================================================
-- 5. PROCEDIMIENTOS (CRUD) Y FUNCIONES DE LISTADO
-- =========================================================

-- ---------- DESTINATARIO (CRUD) ----------
DROP PROCEDURE IF EXISTS sp_insertar_destinatario;
DROP PROCEDURE IF EXISTS sp_actualizar_destinatario;
DROP PROCEDURE IF EXISTS sp_eliminar_destinatario;

CREATE OR REPLACE PROCEDURE sp_insertar_destinatario(
  p_ruc CHAR(11),
  p_nombre VARCHAR(120),
  p_numero_telefono VARCHAR(20),
  p_calle_direccion VARCHAR(120),
  p_codigo_ubigeo VARCHAR(6),
  p_gmail VARCHAR(120)
)
LANGUAGE SQL
AS $$
  INSERT INTO destinatario(
    ruc, nombre, numero_telefono, calle_direccion, codigo_ubigeo, gmail
  )
  VALUES ($1,$2,$3,$4,$5,$6);
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_destinatario(
  p_ruc CHAR(11),
  p_nombre VARCHAR(120),
  p_numero_telefono VARCHAR(20),
  p_calle_direccion VARCHAR(120),
  p_codigo_ubigeo VARCHAR(6),
  p_gmail VARCHAR(120)
)
LANGUAGE SQL
AS $$
  UPDATE destinatario
     SET nombre          = $2,
         numero_telefono = $3,
         calle_direccion = $4,
         codigo_ubigeo   = $5,
         gmail           = $6
   WHERE ruc = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_destinatario(
  p_ruc CHAR(11)
)
LANGUAGE SQL
AS $$
  DELETE FROM destinatario
   WHERE ruc = $1;
$$;

DROP FUNCTION IF EXISTS sp_listar_destinatarios();
CREATE OR REPLACE FUNCTION sp_listar_destinatarios()
RETURNS TABLE(
  ruc CHAR(11),
  nombre VARCHAR(120),
  numero_telefono VARCHAR(20),
  calle_direccion VARCHAR(120),
  codigo_ubigeo VARCHAR(6),
  gmail VARCHAR(120)
) LANGUAGE SQL AS $$
  SELECT
    ruc,
    nombre,
    numero_telefono,
    calle_direccion,
    codigo_ubigeo,
    gmail
  FROM destinatario
  WHERE coalesce(eliminado, false) = false;
$$;

-- ---------- REMITENTE (CRUD) ----------
DROP PROCEDURE IF EXISTS sp_insertar_remitente;
DROP PROCEDURE IF EXISTS sp_actualizar_remitente;
DROP PROCEDURE IF EXISTS sp_eliminar_remitente;

CREATE OR REPLACE PROCEDURE sp_insertar_remitente (
  p_ruc CHAR(11),
  p_nombre_empresa VARCHAR(120),
  p_razon_social VARCHAR(120),
  p_telefono VARCHAR(20),
  p_email VARCHAR(120),
  p_calle_direccion VARCHAR(120),
  p_codigo_ubigeo VARCHAR(6)
)
LANGUAGE SQL
AS $$
  INSERT INTO remitente(
    ruc, nombre_empresa, razon_social,
    telefono, email, calle_direccion, codigo_ubigeo
  )
  VALUES ($1,$2,$3,$4,$5,$6,$7);
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_remitente (
  p_ruc CHAR(11),
  p_nombre_empresa VARCHAR(120),
  p_razon_social VARCHAR(120),
  p_telefono VARCHAR(20),
  p_email VARCHAR(120),
  p_calle_direccion VARCHAR(120),
  p_codigo_ubigeo VARCHAR(6)
)
LANGUAGE SQL
AS $$
  UPDATE remitente
     SET nombre_empresa  = $2,
         razon_social    = $3,
         telefono        = $4,
         email           = $5,
         calle_direccion = $6,
         codigo_ubigeo   = $7
   WHERE ruc = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_remitente (
  p_ruc CHAR(11)
)
LANGUAGE SQL
AS $$
  DELETE FROM remitente
   WHERE ruc = $1;
$$;

DROP FUNCTION IF EXISTS sp_listar_remitentes();
CREATE OR REPLACE FUNCTION sp_listar_remitentes()
RETURNS TABLE(
  ruc CHAR(11),
  nombre_empresa VARCHAR(120),
  razon_social VARCHAR(120),
  telefono VARCHAR(20),
  email VARCHAR(120),
  calle_direccion VARCHAR(120),
  codigo_ubigeo VARCHAR(6)
) LANGUAGE SQL AS $$
  SELECT ruc, nombre_empresa, razon_social, telefono, email, calle_direccion, codigo_ubigeo
    FROM remitente
   ORDER BY nombre_empresa;
$$;

DROP FUNCTION IF EXISTS sp_buscar_remitente(TEXT);
CREATE OR REPLACE FUNCTION sp_buscar_remitente(p_ruc TEXT)
RETURNS TABLE(
  ruc CHAR(11),
  nombre_empresa VARCHAR(120),
  razon_social VARCHAR(120),
  telefono VARCHAR(20),
  email VARCHAR(120),
  calle_direccion VARCHAR(120),
  codigo_ubigeo VARCHAR(6)
) LANGUAGE SQL AS $$
  SELECT ruc, nombre_empresa, razon_social, telefono, email, calle_direccion, codigo_ubigeo
    FROM remitente
   WHERE ruc = p_ruc
   LIMIT 1;
$$;

-- ---------- PRODUCTO (CRUD) ----------
DROP PROCEDURE IF EXISTS sp_insertar_producto;
DROP PROCEDURE IF EXISTS sp_actualizar_producto;
DROP PROCEDURE IF EXISTS sp_eliminar_producto;

CREATE OR REPLACE PROCEDURE sp_insertar_producto (
  p_codigo_producto VARCHAR(20),
  p_nombre_producto VARCHAR(120),
  p_precio_base NUMERIC(10,2),
  p_unidad_medida VARCHAR(20)
)
LANGUAGE SQL
AS $$
  INSERT INTO producto(
    codigo_producto, nombre_producto, precio_base, unidad_medida
  )
  VALUES ($1,$2,$3,$4);
$$;

DROP FUNCTION IF EXISTS sp_listar_productos();
CREATE OR REPLACE FUNCTION sp_listar_productos()
RETURNS TABLE(
  codigo_producto VARCHAR(20),
  nombre_producto VARCHAR(120),
  precio_base NUMERIC(10,2),
  unidad_medida VARCHAR(20)
) LANGUAGE SQL AS $$
  SELECT codigo_producto, nombre_producto, precio_base, unidad_medida
    FROM producto
   ORDER BY nombre_producto;
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_producto (
  p_codigo_producto VARCHAR(20),
  p_nombre_producto VARCHAR(120),
  p_precio_base NUMERIC(10,2),
  p_unidad_medida VARCHAR(20)
)
LANGUAGE SQL
AS $$
  UPDATE producto
     SET nombre_producto = $2,
         precio_base     = $3,
         unidad_medida   = $4
   WHERE codigo_producto = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_producto (
  p_codigo_producto VARCHAR(20)
)
LANGUAGE SQL
AS $$
  DELETE FROM producto
   WHERE codigo_producto = $1;
$$;

-- ---------- UBIGEO (CRUD) ----------
DROP FUNCTION IF EXISTS sp_listar_ubigeos();
CREATE OR REPLACE FUNCTION sp_listar_ubigeos()
RETURNS TABLE(
  codigo_ubigeo VARCHAR(6),
  departamento VARCHAR(100),
  provincia VARCHAR(100),
  distrito VARCHAR(100)
) LANGUAGE SQL AS $$
  SELECT codigo_ubigeo, departamento, provincia, distrito
    FROM ubigeo
   ORDER BY departamento, provincia, distrito;
$$;

-- ---------- VEHICULO (CRUD) ----------
DROP PROCEDURE IF EXISTS sp_insertar_vehiculo;
DROP PROCEDURE IF EXISTS sp_actualizar_vehiculo;
DROP PROCEDURE IF EXISTS sp_eliminar_vehiculo;

CREATE OR REPLACE PROCEDURE sp_insertar_vehiculo (
  p_placa VARCHAR(10),
  p_numero_mtc VARCHAR(20),
  p_tipo_vehiculo VARCHAR(50),
  p_marca VARCHAR(50),
  p_modelo VARCHAR(50),
  p_carga_max NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  INSERT INTO vehiculo(placa, numero_mtc, tipo_vehiculo, marca, modelo, carga_max)
  VALUES ($1,$2,$3,$4,$5,$6);
$$;

DROP FUNCTION IF EXISTS sp_listar_vehiculos();
CREATE OR REPLACE FUNCTION sp_listar_vehiculos()
RETURNS TABLE(
  placa VARCHAR(10),
  numero_mtc VARCHAR(20),
  tipo_vehiculo VARCHAR(50),
  marca VARCHAR(50),
  modelo VARCHAR(50),
  carga_max NUMERIC(10,2)
) LANGUAGE SQL AS $$
  SELECT placa, numero_mtc, tipo_vehiculo, marca, modelo, carga_max
    FROM vehiculo
   ORDER BY placa;
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_vehiculo (
  p_placa VARCHAR(10),
  p_numero_mtc VARCHAR(20),
  p_tipo_vehiculo VARCHAR(50),
  p_marca VARCHAR(50),
  p_modelo VARCHAR(50),
  p_carga_max NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  UPDATE vehiculo
     SET numero_mtc    = $2,
         tipo_vehiculo = $3,
         marca         = $4,
         modelo        = $5,
         carga_max     = $6
   WHERE placa = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_vehiculo (
  p_placa VARCHAR(10)
)
LANGUAGE SQL
AS $$
  DELETE FROM vehiculo
   WHERE placa = $1;
$$;

-- ---------- CONDUCTOR (CRUD) ----------
DROP PROCEDURE IF EXISTS sp_insertar_conductor;
DROP PROCEDURE IF EXISTS sp_actualizar_conductor;
DROP PROCEDURE IF EXISTS sp_eliminar_conductor;

CREATE OR REPLACE PROCEDURE sp_insertar_conductor (
  p_licencia VARCHAR(15),
  p_dni CHAR(8),
  p_nombre VARCHAR(120),
  p_telefono VARCHAR(20),
  p_fecha_vencimiento DATE
)
LANGUAGE SQL
AS $$
  INSERT INTO conductor (
    licencia, dni, nombre, telefono, fecha_vencimiento_licencia
  )
  VALUES ($1,$2,$3,$4,$5);
$$;

DROP FUNCTION IF EXISTS sp_listar_conductores();
CREATE OR REPLACE FUNCTION sp_listar_conductores()
RETURNS TABLE(
  licencia VARCHAR(15),
  dni CHAR(8),
  nombre VARCHAR(120),
  telefono VARCHAR(20),
  fecha_vencimiento_licencia DATE
) LANGUAGE SQL AS $$
  SELECT licencia, dni, nombre, telefono, fecha_vencimiento_licencia
    FROM conductor
   ORDER BY nombre;
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_conductor (
  p_licencia VARCHAR(15),
  p_dni CHAR(8),
  p_nombre VARCHAR(120),
  p_telefono VARCHAR(20),
  p_fecha_vencimiento DATE
)
LANGUAGE SQL
AS $$
  UPDATE conductor
     SET dni                        = $2,
         nombre                     = $3,
         telefono                   = $4,
         fecha_vencimiento_licencia = $5
   WHERE licencia = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_conductor (
  p_licencia VARCHAR(15)
)
LANGUAGE SQL
AS $$
  DELETE FROM conductor
   WHERE licencia = $1;
$$;

-- ---------- ORDEN_DE_PAGO + DETALLE_ORDEN ----------
DROP PROCEDURE IF EXISTS sp_crear_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_actualizar_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_eliminar_orden_de_pago;
DROP PROCEDURE IF EXISTS sp_agregar_detalle_orden;

CREATE OR REPLACE PROCEDURE sp_crear_orden_de_pago (
  p_codigo_orden VARCHAR(15),
  p_fecha DATE,
  p_ruc_cliente CHAR(11),
  p_estado VARCHAR(20)
)
LANGUAGE SQL
AS $$
  INSERT INTO orden_de_pago (
    codigo_orden, fecha, ruc_cliente, estado
  )
  VALUES ($1,$2,$3,$4);
$$;

DROP FUNCTION IF EXISTS sp_listar_ordenes();
CREATE OR REPLACE FUNCTION sp_listar_ordenes()
RETURNS TABLE(
  codigo_orden VARCHAR(15),
  fecha DATE,
  ruc_cliente CHAR(11),
  estado VARCHAR(20)
) LANGUAGE SQL AS $$
  SELECT codigo_orden, fecha, ruc_cliente, estado
    FROM orden_de_pago
   ORDER BY fecha DESC;
$$;

DROP PROCEDURE IF EXISTS sp_eliminar_detalle_por_orden(VARCHAR);
CREATE OR REPLACE PROCEDURE sp_eliminar_detalle_por_orden(p_codigo_orden VARCHAR)
LANGUAGE SQL
AS $$
  DELETE FROM detalle_orden WHERE codigo_orden = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_orden_de_pago (
  p_codigo_orden VARCHAR(15),
  p_fecha DATE,
  p_ruc_cliente CHAR(11),
  p_estado VARCHAR(20)
)
LANGUAGE SQL
AS $$
  UPDATE orden_de_pago
     SET fecha       = $2,
         ruc_cliente = $3,
         estado      = $4
   WHERE codigo_orden = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_orden_de_pago (
  p_codigo_orden VARCHAR(15)
)
LANGUAGE plpgsql
AS $$
BEGIN
  DELETE FROM detalle_orden
   WHERE codigo_orden = p_codigo_orden;

  DELETE FROM orden_de_pago
   WHERE codigo_orden = p_codigo_orden;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_agregar_detalle_orden (
  p_codigo_orden VARCHAR(15),
  p_codigo_producto VARCHAR(20),
  p_cantidad NUMERIC(10,2),
  p_precio_unitario NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  INSERT INTO detalle_orden (
    codigo_orden, codigo_producto, cantidad, precio_unitario, subtotal
  )
  VALUES ($1,$2,$3,$4,$3*$4);
$$;


-- ---------- BIEN_TRANSPORTABLE + DETALLE_BIENES ----------
DROP PROCEDURE IF EXISTS sp_insertar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_actualizar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_eliminar_bien_transportable;
DROP PROCEDURE IF EXISTS sp_agregar_detalle_bien_guia;

CREATE OR REPLACE PROCEDURE sp_insertar_bien_transportable (
  p_codigo_bien VARCHAR(20),
  p_descripcion VARCHAR(120),
  p_unidad_medida VARCHAR(20),
  p_peso_unitario_promedio NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  INSERT INTO bien_transportable (
    codigo_bien, descripcion, unidad_medida, peso_unitario_promedio
  )
  VALUES ($1,$2,$3,$4);
$$;

DROP PROCEDURE IF EXISTS sp_actualizar_estado_guia(VARCHAR, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_actualizar_estado_guia(p_codigo_guia VARCHAR, p_nuevo_estado VARCHAR)
LANGUAGE SQL
AS $$
  UPDATE cabecera_guia
     SET estado_guia = $2
   WHERE codigo_guia = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_bien_transportable (
  p_codigo_bien VARCHAR(20),
  p_descripcion VARCHAR(120),
  p_unidad_medida VARCHAR(20),
  p_peso_unitario_promedio NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  UPDATE bien_transportable
     SET descripcion            = $2,
         unidad_medida          = $3,
         peso_unitario_promedio = $4
   WHERE codigo_bien = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_bien_transportable (
  p_codigo_bien VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
BEGIN
  DELETE FROM detalle_bienes
   WHERE codigo_bien = p_codigo_bien;

  DELETE FROM bien_transportable
   WHERE codigo_bien = p_codigo_bien;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_agregar_detalle_bien_guia (
  p_codigo_guia VARCHAR(15),
  p_codigo_bien VARCHAR(20),
  p_cantidad NUMERIC(10,2),
  p_peso_total NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  INSERT INTO detalle_bienes(
    codigo_guia, codigo_bien, cantidad, peso_total
  )
  VALUES ($1,$2,$3,$4);
$$;


-- =========================================================
-- 6. PROCESOS DE GUÍA Y TRASLADO
-- =========================================================

-- EMITIR GUÍA (cabecera + cuerpo, con fecha/hora actuales)
DROP PROCEDURE IF EXISTS sp_emitir_guia;

CREATE OR REPLACE PROCEDURE sp_emitir_guia(
  p_codigo_guia      VARCHAR(15),
  p_serie            VARCHAR(10),
  p_numero           VARCHAR(10),
  p_cod_orden        VARCHAR(15),
  p_ruc_remitente    CHAR(11),
  p_ruc_destinatario CHAR(11),
  p_dir_partida      VARCHAR(120),
  p_dir_llegada      VARCHAR(120),
  p_ubigeo_origen    VARCHAR(6),
  p_ubigeo_destino   VARCHAR(6),
  p_motivo           VARCHAR(100),
  p_modalidad        VARCHAR(50),
  p_peso_total       NUMERIC(10,2),
  p_numero_bultos    INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO cabecera_guia(
    codigo_guia, serie, numero, cod_orden,
    ruc_remitente, fecha_emision, hora_emision, estado_guia
  )
  VALUES(
    p_codigo_guia, p_serie, p_numero, p_cod_orden,
    p_ruc_remitente, CURRENT_DATE, CURRENT_TIME, 'emitida'
  );

  INSERT INTO cuerpo_guia(
    codigo_guia, ruc_destinatario, direccion_partida,
    direccion_llegada, ubigeo_origen, ubigeo_destino,
    motivo_traslado, modalidad_transporte, peso_total, numero_bultos
  )
  VALUES(
    p_codigo_guia, p_ruc_destinatario, p_dir_partida,
    p_dir_llegada, p_ubigeo_origen, p_ubigeo_destino,
    p_motivo, p_modalidad, p_peso_total, p_numero_bultos
  );
END;
$$;

-- GENERAR GUÍA (cabecera + cuerpo + traslado) – versión de tu primer bloque
DROP PROCEDURE IF EXISTS sp_generar_guia;

CREATE OR REPLACE PROCEDURE sp_generar_guia (
  p_codigo_guia        VARCHAR(15),
  p_serie              VARCHAR(10),
  p_numero             VARCHAR(10),
  p_cod_orden          VARCHAR(15),
  p_ruc_remitente      CHAR(11),
  p_fecha_emision      DATE,
  p_hora_emision       TIME,
  p_estado_guia        VARCHAR(20),

  p_ruc_destinatario   CHAR(11),
  p_direccion_partida  VARCHAR(120),
  p_direccion_llegada  VARCHAR(120),
  p_ubigeo_origen      VARCHAR(6),
  p_ubigeo_destino     VARCHAR(6),
  p_motivo_traslado    VARCHAR(100),
  p_modalidad_transporte VARCHAR(50),
  p_peso_total         NUMERIC(10,2),
  p_numero_bultos      INTEGER,

  p_codigo_traslado    VARCHAR(15),
  p_placa              VARCHAR(10),
  p_licencia           VARCHAR(15),
  p_fecha_inicio       TIMESTAMP,
  p_fecha_fin          TIMESTAMP,
  p_estado_traslado    VARCHAR(20),
  p_observaciones      TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
  -- CABECERA
  INSERT INTO cabecera_guia (
    codigo_guia, serie, numero, cod_orden,
    ruc_remitente, fecha_emision, hora_emision, estado_guia
  )
  VALUES (
    p_codigo_guia, p_serie, p_numero, p_cod_orden,
    p_ruc_remitente, p_fecha_emision, p_hora_emision, p_estado_guia
  );

  -- CUERPO
  INSERT INTO cuerpo_guia (
    codigo_guia, ruc_destinatario, direccion_partida,
    direccion_llegada, ubigeo_origen, ubigeo_destino,
    motivo_traslado, modalidad_transporte, peso_total, numero_bultos
  )
  VALUES (
    p_codigo_guia, p_ruc_destinatario, p_direccion_partida,
    p_direccion_llegada, p_ubigeo_origen, p_ubigeo_destino,
    p_motivo_traslado, p_modalidad_transporte, p_peso_total, p_numero_bultos
  );

  -- TRASLADO
  INSERT INTO traslado (
    codigo_traslado, codigo_guia, placa, licencia,
    fecha_inicio, fecha_fin, estado_traslado, observaciones
  )
  VALUES (
    p_codigo_traslado, p_codigo_guia, p_placa, p_licencia,
    p_fecha_inicio, p_fecha_fin, p_estado_traslado, p_observaciones
  );
END;
$$;

-- LISTAR GUÍAS
DROP FUNCTION IF EXISTS sp_listar_guias();
CREATE OR REPLACE FUNCTION sp_listar_guias()
RETURNS TABLE(
  codigo_guia        VARCHAR(15),
  serie              VARCHAR(10),
  numero             VARCHAR(10),
  cod_orden          VARCHAR(15),
  fecha_emision      DATE,
  estado_guia        VARCHAR(20),
  destinatario       VARCHAR(120),
  direccion_llegada  VARCHAR(120)
) LANGUAGE SQL AS $$
  SELECT
    g.codigo_guia,
    g.serie,
    g.numero,
    g.cod_orden,
    g.fecha_emision,
    g.estado_guia,
    d.nombre AS destinatario,
    cg.direccion_llegada
  FROM cabecera_guia g
  LEFT JOIN cuerpo_guia cg ON cg.codigo_guia = g.codigo_guia
  LEFT JOIN destinatario d ON d.ruc = cg.ruc_destinatario
  ORDER BY g.fecha_emision DESC, g.codigo_guia;
$$;

-- TRASLADO: registrar / actualizar / eliminar / listar
DROP PROCEDURE IF EXISTS sp_registrar_traslado;
DROP PROCEDURE IF EXISTS sp_actualizar_traslado;
DROP PROCEDURE IF EXISTS sp_eliminar_traslado;

CREATE OR REPLACE PROCEDURE sp_registrar_traslado(
  p_codigo_traslado VARCHAR(15),
  p_codigo_guia     VARCHAR(15),
  p_placa           VARCHAR(10),
  p_licencia        VARCHAR(15),
  p_fecha_inicio    TIMESTAMP,
  p_fecha_fin       TIMESTAMP,
  p_estado          VARCHAR(20),
  p_observaciones   TEXT
)
LANGUAGE SQL
AS $$
  INSERT INTO traslado(
    codigo_traslado, codigo_guia, placa, licencia,
    fecha_inicio, fecha_fin, estado_traslado, observaciones
  )
  VALUES(
    $1, $2, $3, $4,
    $5, $6, $7, $8
  );
$$;

CREATE OR REPLACE PROCEDURE sp_actualizar_traslado(
  p_codigo_traslado VARCHAR(15),
  p_codigo_guia     VARCHAR(15),
  p_placa           VARCHAR(10),
  p_licencia        VARCHAR(15),
  p_fecha_inicio    TIMESTAMP,
  p_fecha_fin       TIMESTAMP,
  p_estado          VARCHAR(20),
  p_observaciones   TEXT
)
LANGUAGE SQL
AS $$
  UPDATE traslado
     SET codigo_guia     = $2,
         placa           = $3,
         licencia        = $4,
         fecha_inicio    = $5,
         fecha_fin       = $6,
         estado_traslado = $7,
         observaciones   = $8
   WHERE codigo_traslado = $1;
$$;

CREATE OR REPLACE PROCEDURE sp_eliminar_traslado(
  p_codigo_traslado VARCHAR(15)
)
LANGUAGE SQL
AS $$
  DELETE FROM traslado
   WHERE codigo_traslado = $1;
$$;

-- ACTUALIZAR SOLO ESTADO DEL TRASLADO
DROP PROCEDURE IF EXISTS sp_actualizar_estado_traslado;
CREATE OR REPLACE PROCEDURE sp_actualizar_estado_traslado(
  p_codigo_traslado VARCHAR(15),
  p_nuevo_estado    VARCHAR(20)
)
LANGUAGE SQL
AS $$
  UPDATE traslado
     SET estado_traslado = $2
   WHERE codigo_traslado = $1;
$$;

-- LISTAR TRASLADOS (con guía, vehículo y conductor)
DROP FUNCTION IF EXISTS sp_listar_traslados();
CREATE OR REPLACE FUNCTION sp_listar_traslados()
RETURNS TABLE(
  codigo_traslado VARCHAR(15),
  codigo_guia     VARCHAR(15),
  fecha_emision   DATE,
  placa           VARCHAR(10),
  tipo_vehiculo   VARCHAR(50),
  licencia        VARCHAR(15),
  conductor       VARCHAR(120),
  fecha_inicio    TIMESTAMP,
  fecha_fin       TIMESTAMP,
  estado_traslado VARCHAR(20),
  observaciones   TEXT
) LANGUAGE SQL AS $$
  SELECT
    t.codigo_traslado,
    t.codigo_guia,
    g.fecha_emision,
    t.placa,
    v.tipo_vehiculo,
    t.licencia,
    c.nombre AS conductor,
    t.fecha_inicio,
    t.fecha_fin,
    t.estado_traslado,
    t.observaciones
  FROM traslado t
  LEFT JOIN cabecera_guia g ON g.codigo_guia = t.codigo_guia
  LEFT JOIN vehiculo v      ON v.placa = t.placa
  LEFT JOIN conductor c     ON c.licencia = t.licencia
  ORDER BY t.fecha_inicio DESC;
$$;

-- CONFIRMAR ENTREGA DE GUÍA (estado_guia y traslado)
DROP PROCEDURE IF EXISTS sp_confirmar_entrega;
CREATE OR REPLACE PROCEDURE sp_confirmar_entrega(
  p_codigo_guia VARCHAR(15)
)
LANGUAGE SQL
AS $$
  UPDATE cabecera_guia
     SET estado_guia = 'entregada'
   WHERE codigo_guia = $1;

  UPDATE traslado
     SET estado_traslado = 'entregado',
         fecha_fin       = COALESCE(fecha_fin, NOW())
   WHERE codigo_guia = $1;
$$;


-- =========================================================
-- 7. REPORTES (10 + 1 historial)
-- =========================================================

-- 1) Detalle de una orden con sus productos
DROP FUNCTION IF EXISTS sp_reporte_detalle_orden(VARCHAR);
CREATE OR REPLACE FUNCTION sp_reporte_detalle_orden(
  p_codigo_orden VARCHAR(15)
)
RETURNS TABLE(
  codigo_orden    VARCHAR(15),
  codigo_producto VARCHAR(20),
  nombre_producto VARCHAR(120),
  cantidad        NUMERIC(10,2),
  precio_unitario NUMERIC(10,2),
  subtotal        NUMERIC(10,2)
)
LANGUAGE SQL
AS $$
  SELECT
    o.codigo_orden,
    p.codigo_producto,
    p.nombre_producto,
    d.cantidad,
    d.precio_unitario,
    d.subtotal
  FROM orden_de_pago o
  JOIN detalle_orden d ON d.codigo_orden = o.codigo_orden
  JOIN producto p      ON p.codigo_producto = d.codigo_producto
  WHERE o.codigo_orden = p_codigo_orden;
$$;

-- 2) Guías emitidas por fecha y estado
DROP FUNCTION IF EXISTS sp_reporte_guias_por_fecha_estado();
CREATE OR REPLACE FUNCTION sp_reporte_guias_por_fecha_estado()
RETURNS TABLE(
  fecha_emision  DATE,
  estado_guia    VARCHAR(20),
  guias          BIGINT
) LANGUAGE SQL AS $$
  SELECT
    fecha_emision,
    estado_guia,
    COUNT(*)::BIGINT AS guias
  FROM cabecera_guia
  GROUP BY fecha_emision, estado_guia
  ORDER BY fecha_emision DESC, estado_guia;
$$;

-- 3) Productos más vendidos por cantidad
DROP FUNCTION IF EXISTS sp_reporte_productos_mas_vendidos();
CREATE OR REPLACE FUNCTION sp_reporte_productos_mas_vendidos()
RETURNS TABLE(
  codigo_producto   VARCHAR(20),
  nombre_producto   VARCHAR(120),
  cantidad_vendida  NUMERIC
) LANGUAGE SQL AS $$
  SELECT
    p.codigo_producto,
    p.nombre_producto,
    SUM(d.cantidad) AS cantidad_vendida
  FROM producto p
  JOIN detalle_orden d ON d.codigo_producto = p.codigo_producto
  GROUP BY p.codigo_producto, p.nombre_producto
  ORDER BY cantidad_vendida DESC;
$$;

-- 4) Utilización de vehículos (viajes por placa)
DROP FUNCTION IF EXISTS sp_reporte_utilizacion_vehiculos();
CREATE OR REPLACE FUNCTION sp_reporte_utilizacion_vehiculos()
RETURNS TABLE(
  placa  VARCHAR(10),
  marca  VARCHAR(50),
  modelo VARCHAR(50),
  viajes BIGINT
) LANGUAGE SQL AS $$
  SELECT
    v.placa,
    v.marca,
    v.modelo,
    COUNT(t.codigo_traslado)::BIGINT AS viajes
  FROM vehiculo v
  LEFT JOIN traslado t ON t.placa = v.placa
  GROUP BY v.placa, v.marca, v.modelo
  ORDER BY viajes DESC;
$$;

-- 5) Licencias por vencer en N días
DROP FUNCTION IF EXISTS sp_reporte_licencias_por_vencer(INT);
CREATE OR REPLACE FUNCTION sp_reporte_licencias_por_vencer(
  p_dias INT
)
RETURNS TABLE(
  licencia VARCHAR(15),
  nombre   VARCHAR(120),
  telefono VARCHAR(20),
  fecha_vencimiento_licencia DATE
) LANGUAGE SQL AS $$
  SELECT
    licencia,
    nombre,
    telefono,
    fecha_vencimiento_licencia
  FROM conductor
  WHERE fecha_vencimiento_licencia <= CURRENT_DATE + (p_dias * INTERVAL '1 day')
  ORDER BY fecha_vencimiento_licencia;
$$;

-- 6) Guías sin traslado asignado
DROP FUNCTION IF EXISTS sp_reporte_guias_sin_traslado();
CREATE OR REPLACE FUNCTION sp_reporte_guias_sin_traslado()
RETURNS TABLE(
  codigo_guia   VARCHAR(15),
  fecha_emision DATE,
  estado_guia   VARCHAR(20)
) LANGUAGE SQL AS $$
  SELECT
    cg.codigo_guia,
    cg.fecha_emision,
    cg.estado_guia
  FROM cabecera_guia cg
  LEFT JOIN traslado t ON t.codigo_guia = cg.codigo_guia
  WHERE t.codigo_guia IS NULL
  ORDER BY cg.fecha_emision DESC;
$$;

-- 7) Órdenes de pago por cliente (conteo)
DROP FUNCTION IF EXISTS sp_reporte_ordenes_por_cliente();
CREATE OR REPLACE FUNCTION sp_reporte_ordenes_por_cliente()
RETURNS TABLE(
  ruc           CHAR(11),
  nombre        VARCHAR(120),
  total_ordenes BIGINT
) LANGUAGE SQL AS $$
  SELECT
    d.ruc,
    d.nombre,
    COUNT(o.codigo_orden)::BIGINT AS total_ordenes
  FROM destinatario d
  LEFT JOIN orden_de_pago o ON o.ruc_cliente = d.ruc
  GROUP BY d.ruc, d.nombre
  ORDER BY total_ordenes DESC;
$$;

-- 8) Bultos entregados por cliente (últimos 90 días)
DROP FUNCTION IF EXISTS sp_reporte_bultos_por_cliente_90d();
CREATE OR REPLACE FUNCTION sp_reporte_bultos_por_cliente_90d()
RETURNS TABLE(
  cliente   VARCHAR(120),
  bultos_90d BIGINT
) LANGUAGE SQL AS $$
  SELECT
    d.nombre AS cliente,
    COALESCE(SUM(cg.numero_bultos),0)::BIGINT AS bultos_90d
  FROM cuerpo_guia cg
  JOIN destinatario d    ON d.ruc = cg.ruc_destinatario
  JOIN cabecera_guia cab ON cab.codigo_guia = cg.codigo_guia
  WHERE cab.fecha_emision >= CURRENT_DATE - INTERVAL '90 days'
  GROUP BY d.nombre
  ORDER BY bultos_90d DESC;
$$;

-- 9) KPI diario de estado de guías
DROP FUNCTION IF EXISTS sp_reporte_kpi_guias_diario();
CREATE OR REPLACE FUNCTION sp_reporte_kpi_guias_diario()
RETURNS TABLE(
  fecha_emision DATE,
  emitidas      BIGINT,
  en_transito   BIGINT,
  entregadas    BIGINT
) LANGUAGE SQL AS $$
  SELECT
    cab.fecha_emision,
    SUM(CASE WHEN cab.estado_guia = 'emitida'       THEN 1 ELSE 0 END)::BIGINT AS emitidas,
    SUM(CASE WHEN t.estado_traslado = 'en tránsito' THEN 1 ELSE 0 END)::BIGINT AS en_transito,
    SUM(CASE WHEN t.estado_traslado = 'entregado'   THEN 1 ELSE 0 END)::BIGINT AS entregadas
  FROM cabecera_guia cab
  LEFT JOIN traslado t ON t.codigo_guia = cab.codigo_guia
  GROUP BY cab.fecha_emision
  ORDER BY cab.fecha_emision DESC;
$$;

-- 10) Ingresos (subtotal) por fecha de orden
DROP FUNCTION IF EXISTS sp_reporte_ingresos_por_fecha();

CREATE OR REPLACE FUNCTION sp_reporte_ingresos_por_fecha()
RETURNS TABLE(
  fecha     DATE,
  total_dia NUMERIC(12,2)
)
LANGUAGE SQL
AS $$
  SELECT
    o.fecha,
    SUM(d.subtotal)::NUMERIC(12,2) AS total_dia
  FROM orden_de_pago o
  JOIN detalle_orden d ON d.codigo_orden = o.codigo_orden
  GROUP BY o.fecha
  ORDER BY o.fecha DESC;
$$;


-- 11) Historial de cambios de estado de traslado
DROP FUNCTION IF EXISTS sp_reporte_historial_estados_traslado();
CREATE OR REPLACE FUNCTION sp_reporte_historial_estados_traslado()
RETURNS TABLE(
  codigo_traslado VARCHAR(15),
  codigo_guia     VARCHAR(15),
  estado_anterior VARCHAR(20),
  estado_nuevo    VARCHAR(20),
  fecha_cambio    TIMESTAMP
) LANGUAGE SQL AS $$
  SELECT
    t.codigo_traslado,
    t.codigo_guia,
    log.estado_anterior,
    log.estado_nuevo,
    log.fecha_cambio
  FROM traslado_log_estado log
  JOIN traslado t ON t.codigo_traslado = log.codigo_traslado
  ORDER BY log.fecha_cambio DESC;
$$;



SELECT rolname FROM pg_roles;
ALTER TABLE cabecera_guia ADD COLUMN IF NOT EXISTS eliminado boolean DEFAULT false;
ALTER TABLE destinatario   ADD COLUMN IF NOT EXISTS eliminado boolean DEFAULT false;
ALTER TABLE traslado       ADD COLUMN IF NOT EXISTS eliminado boolean DEFAULT false;

ALTER TABLE cabecera_guia
  ADD COLUMN IF NOT EXISTS ruc_destinatario varchar,
  ADD COLUMN IF NOT EXISTS dir_partida text,
  ADD COLUMN IF NOT EXISTS dir_llegada text,
  ADD COLUMN IF NOT EXISTS ubigeo_origen varchar,
  ADD COLUMN IF NOT EXISTS ubigeo_destino varchar,
  ADD COLUMN IF NOT EXISTS peso_total double precision DEFAULT 0,
  ADD COLUMN IF NOT EXISTS numero_bultos integer DEFAULT 0,
  ADD COLUMN IF NOT EXISTS estado_guia varchar;
