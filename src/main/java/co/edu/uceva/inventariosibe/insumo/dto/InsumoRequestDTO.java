package co.edu.uceva.inventariosibe.insumo.dto;

import co.edu.uceva.inventariosibe.insumo.TipoInsumo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class InsumoRequestDTO {

    @NotBlank
    private String nombre;

    @NotBlank
    private String presentacion;

    @NotBlank
    private String unidadMedida;

    @PositiveOrZero
    private int stockMinimo;

    @NotNull
    private Boolean activo;

    @NotBlank
    private String marca;

    @NotNull
    private TipoInsumo tipo;

    @Positive
    private int unidadesPorCaja;

    public int getUnidadesPorCaja() {
        return unidadesPorCaja;
    }

    public void setUnidadesPorCaja(int unidadesPorCaja) {
        this.unidadesPorCaja = unidadesPorCaja;
    }

    private String registroInvima;

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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public TipoInsumo getTipo() {
        return tipo;
    }

    public void setTipo(TipoInsumo tipo) {
        this.tipo = tipo;
    }

    public String getRegistroInvima() {
        return registroInvima;
    }

    public void setRegistroInvima(String registroInvima) {
        this.registroInvima = registroInvima;
    }
}