-- Agrega baja lógica al lote (habilitar/deshabilitar) sin tocar stock ni trazabilidad.
-- Un lote inactivo se excluye de dashboards y de salidas, pero conserva sus movimientos.
ALTER TABLE "lote" ADD COLUMN "activo" boolean NOT NULL DEFAULT true;

COMMENT ON COLUMN "lote"."activo" IS 'Baja lógica del lote (vencido, dañado, retenido). No se borra el registro.';
