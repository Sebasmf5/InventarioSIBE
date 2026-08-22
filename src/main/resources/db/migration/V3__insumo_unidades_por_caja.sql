ALTER TABLE "insumo" ADD COLUMN "unidades_por_caja" int NOT NULL DEFAULT 1;
COMMENT ON COLUMN "insumo"."unidades_por_caja" IS 'Factor: 1 caja = N unidades. Default 1.';