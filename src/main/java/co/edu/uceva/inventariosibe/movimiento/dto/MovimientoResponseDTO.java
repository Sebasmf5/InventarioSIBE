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

    private String nombreUsuario;
    private String numeroLote;
    private String nombreInsumo;
    private int unidadesPorCaja;
    private int cajas;
    private int unidadesSueltas;
    private String cantidadFormateada;

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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public int getUnidadesPorCaja() {
        return unidadesPorCaja;
    }

    public void setUnidadesPorCaja(int unidadesPorCaja) {
        this.unidadesPorCaja = unidadesPorCaja;
    }

    public int getCajas() {
        return cajas;
    }

    public void setCajas(int cajas) {
        this.cajas = cajas;
    }

    public int getUnidadesSueltas() {
        return unidadesSueltas;
    }

    public void setUnidadesSueltas(int unidadesSueltas) {
        this.unidadesSueltas = unidadesSueltas;
    }

    public String getCantidadFormateada() {
        return cantidadFormateada;
    }

    public void setCantidadFormateada(String cantidadFormateada) {
        this.cantidadFormateada = cantidadFormateada;
    }
}