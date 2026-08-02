package co.edu.uceva.inventariosibe.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Movimiento {

    private final UUID id;
    private final UUID loteId;
    private final UUID usuarioId;
    private final TipoMovimiento tipo;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final String observacion;

    public Movimiento(UUID id, UUID loteId, UUID usuarioId, TipoMovimiento tipo, int cantidad, LocalDateTime fecha, String observacion) {
        // Validación de dominio exigida
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor a cero.");
        }

        this.id = id;
        this.loteId = loteId;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.observacion = observacion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLoteId() {
        return loteId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getObservacion() {
        return observacion;
    }
}