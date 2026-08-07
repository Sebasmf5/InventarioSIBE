package co.edu.uceva.inventariosibe.lote.dto;

import co.edu.uceva.inventariosibe.lote.Lote;

import java.time.LocalDate;
import java.util.UUID;

public class LoteResponseDTO {

    private UUID id;
    private UUID insumoId;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private int cantidadInicial;
    private int cantidadActual;
    private LocalDate fechaIngreso;
    private String ubicacion;

    public LoteResponseDTO() {
    }

    public LoteResponseDTO(Lote lote) {
        this.id = lote.getId();
        this.insumoId = lote.getInsumoId();
        this.numeroLote = lote.getNumeroLote();
        this.fechaVencimiento = lote.getFechaVencimiento();
        this.cantidadInicial = lote.getCantidadInicial();
        this.cantidadActual = lote.getCantidadActual();
        this.fechaIngreso = lote.getFechaIngreso();
        this.ubicacion = lote.getUbicacion();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(UUID insumoId) {
        this.insumoId = insumoId;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(int cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(int cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}