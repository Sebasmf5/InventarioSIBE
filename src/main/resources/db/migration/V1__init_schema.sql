-- pgcrypto no se necesita: Hibernate genera UUIDs cliente-side con
-- @GeneratedValue(strategy = GenerationType.UUID). En Neon (Postgres 13+)
-- gen_random_uuid() viene built-in en core; en H2 (tests) la extensión no existe.
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE "insumo" (
                           "id" uuid PRIMARY KEY,
                          "nombre" varchar NOT NULL,
                          "presentacion" varchar NOT NULL,
                          "unidad_medida" varchar NOT NULL,
                          "stock_minimo" int NOT NULL DEFAULT 0,
                          "activo" boolean NOT NULL DEFAULT true,
                          "marca" varchar NOT NULL,
                          "tipo" varchar NOT NULL CHECK ("tipo" IN ('MEDICAMENTO', 'INSUMO_MEDICO')),
                          "registro_invima" varchar
);

CREATE TABLE "usuario" (
                           "id" uuid PRIMARY KEY,
                           "nombre" varchar NOT NULL,
                           "email" varchar UNIQUE NOT NULL,
                           "password_hash" varchar NOT NULL,
                           "rol" varchar NOT NULL CHECK ("rol" IN ('ENFERMERIA', 'SUPERVISOR')),
                           "activo" boolean NOT NULL DEFAULT true
);

CREATE TABLE "lote" (
                        "id" uuid PRIMARY KEY,
                        "insumo_id" uuid NOT NULL REFERENCES "insumo" ("id"),
                        "numero_lote" varchar NOT NULL,
                        "fecha_vencimiento" date NOT NULL,
                        "cantidad_inicial" int NOT NULL CHECK ("cantidad_inicial" >= 0),
                        "cantidad_actual" int NOT NULL CHECK ("cantidad_actual" >= 0),
                        "fecha_ingreso" date NOT NULL DEFAULT CURRENT_DATE,
                        "ubicacion" varchar NOT NULL
);

CREATE TABLE "movimiento" (
                              "id" uuid PRIMARY KEY,
                              "lote_id" uuid NOT NULL REFERENCES "lote" ("id"),
                              "usuario_id" uuid NOT NULL REFERENCES "usuario" ("id"),
                              "tipo" varchar NOT NULL CHECK ("tipo" IN ('ENTRADA', 'SALIDA')),
                              "cantidad" int NOT NULL CHECK ("cantidad" > 0),
                              "fecha" timestamp NOT NULL DEFAULT now(),
                              "observacion" varchar
);

CREATE TABLE "configuracion_semaforo" (
                                          "id" uuid PRIMARY KEY,
                                          "dias_verde" int NOT NULL DEFAULT 90,
                                          "dias_amarillo" int NOT NULL DEFAULT 30,
                                          "dias_rojo" int NOT NULL DEFAULT 0
);

-- Índices para las consultas más frecuentes
CREATE INDEX idx_lote_fecha_vencimiento ON "lote" ("fecha_vencimiento");
CREATE INDEX idx_lote_insumo_id ON "lote" ("insumo_id");
CREATE INDEX idx_movimiento_lote_id ON "movimiento" ("lote_id");
CREATE INDEX idx_movimiento_usuario_id ON "movimiento" ("usuario_id");

-- Fila inicial de configuración de semáforo (valores por defecto acordados)
INSERT INTO "configuracion_semaforo" ("id", "dias_verde", "dias_amarillo", "dias_rojo")
VALUES ('00000000-0000-0000-0000-000000000001', 90, 30, 0);

COMMENT ON COLUMN "insumo"."tipo" IS 'MEDICAMENTO o INSUMO_MEDICO';
COMMENT ON COLUMN "movimiento"."tipo" IS 'ENTRADA o SALIDA';
COMMENT ON COLUMN "usuario"."rol" IS 'ENFERMERIA o SUPERVISOR';