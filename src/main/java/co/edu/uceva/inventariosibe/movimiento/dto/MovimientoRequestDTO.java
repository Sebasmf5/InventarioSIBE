package co.edu.uceva.inventariosibe.movimiento.dto;

import co.edu.uceva.inventariosibe.movimiento.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class MovimientoRequestDTO {

    @NotNull
    private UUID loteId;

    @NotNull
    private UUID usuarioId;

    @NotNull
    private TipoMovimiento tipo;

    @Positive
    private int cantidad;

    private String observacion;

    public UUID getLoteId() {
        return loteId;
    }

    public void setLoteId(UUID loteId) {
        this.loteId = loteId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}