package co.edu.uceva.inventariosibe.domain.model;

import java.util.UUID;

public class Medicamento {
    private final UUID id;
    private String nombre;
    private String presentacion;
    private String unidadMedida;
    private int stockMinimo;
    private boolean estado;

    public Medicamento(UUID id, String nombre, String presentacion,
                       String unidadMedida, int stockMinimo, boolean estado) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    public void validarPuedeRecibirLote(){
        if (!this.estado) {
            throw new IllegalStateException(
                    "El medicamento está inactivo y no puede recibir nuevos lotes."
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
