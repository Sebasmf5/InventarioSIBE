package co.edu.uceva.inventariosibe.insumo.dto;

import co.edu.uceva.inventariosibe.insumo.Insumo;
import co.edu.uceva.inventariosibe.insumo.TipoInsumo;
import co.edu.uceva.inventariosibe.lote.EstadoSemaforo;

import java.util.UUID;

public class InsumoResponseDTO {

    private UUID id;
    private String nombre;
    private String presentacion;
    private String unidadMedida;
    private int stockMinimo;
    private boolean activo;
    private String marca;
    private TipoInsumo tipo;
    private String registroInvima;
    private EstadoSemaforo estadoInsumo;

    public InsumoResponseDTO() {
    }

    public InsumoResponseDTO(Insumo insumo, EstadoSemaforo estadoInsumo) {
        this.id = insumo.getId();
        this.nombre = insumo.getNombre();
        this.presentacion = insumo.getPresentacion();
        this.unidadMedida = insumo.getUnidadMedida();
        this.stockMinimo = insumo.getStockMinimo();
        this.activo = insumo.isActivo();
        this.marca = insumo.getMarca();
        this.tipo = insumo.getTipo();
        this.registroInvima = insumo.getRegistroInvima();
        this.estadoInsumo = estadoInsumo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
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

    public EstadoSemaforo getEstadoInsumo() {
        return estadoInsumo;
    }

    public void setEstadoInsumo(EstadoSemaforo estadoInsumo) {
        this.estadoInsumo = estadoInsumo;
    }
}