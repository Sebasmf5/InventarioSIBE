package co.edu.uceva.inventariosibe.movimiento.dto;

import co.edu.uceva.inventariosibe.movimiento.Movimiento;
import co.edu.uceva.inventariosibe.movimiento.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.UUID;

public class MovimientoResponseDTO {

    private UUID id;
    private UUID loteId;
    private UUID usuarioId;
    private TipoMovimiento tipo;
    private int cantidad;
    private LocalDateTime fecha;
    private String observacion;

    public MovimientoResponseDTO() {
    }

    public MovimientoResponseDTO(Movimiento movimiento) {
        this.id = movimiento.getId();
        this.loteId = movimiento.getLoteId();
        this.usuarioId = movimiento.getUsuarioId();
        this.tipo = movimiento.getTipo();
        this.cantidad = movimiento.getCantidad();
        this.fecha = movimiento.getFecha();
        this.observacion = movimiento.getObservacion();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}