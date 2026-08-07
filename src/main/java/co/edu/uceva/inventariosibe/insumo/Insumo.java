package co.edu.uceva.inventariosibe.insumo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "presentacion", nullable = false)
    private String presentacion;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "marca", nullable = false)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoInsumo tipo;

    @Column(name = "registro_invima")
    private String registroInvima;

    protected Insumo() {
    }

    public Insumo(UUID id, String nombre, String presentacion,
                  String unidadMedida, int stockMinimo, boolean activo,
                  String marca, TipoInsumo tipo, String registroInvima) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
        this.marca = marca;
        this.tipo = tipo;
        this.registroInvima = registroInvima;
    }

    public void validarPuedeRecibirLote() {
        if (!this.activo) {
            throw new IllegalStateException(
                    "El insumo está inactivo y no puede recibir nuevos lotes."
            );
        }
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
}