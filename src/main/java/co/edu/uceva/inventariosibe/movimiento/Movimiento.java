package co.edu.uceva.inventariosibe.movimiento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimiento")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lote_id", nullable = false)
    private UUID loteId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "observacion")
    private String observacion;

    protected Movimiento() {
    }

    public Movimiento(UUID id, UUID loteId, UUID usuarioId, TipoMovimiento tipo,
                      int cantidad, LocalDateTime fecha, String observacion) {
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