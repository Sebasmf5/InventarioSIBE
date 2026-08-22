package co.edu.uceva.inventariosibe.lote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "lote")
public class Lote {

    /**
     * Resultado inmutable de descomponer un stock en cajas + unidades sueltas.
     * Un record es una clase ligera que solo guarda datos: Java genera
     * automáticamente el constructor, los accessors cajas()/unidadesSueltas(),
     * equals(), hashCode() y toString().
     */
    public record CajasUnidades(int cajas, int unidadesSueltas) {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "numero_lote", nullable = false)
    private String numeroLote;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "cantidad_inicial", nullable = false)
    private int cantidadInicial; // total de unidades (se calcula con el total de cajas * las unidades por caja)

    @Column(name = "cantidad_actual", nullable = false)
    private int cantidadActual;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "ubicacion", nullable = false)
    private String ubicacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected Lote() {
    }

    public Lote(UUID id, UUID insumoId, String numeroLote, LocalDate fechaVencimiento,
                int cantidadInicial, int cantidadActual, LocalDate fechaIngreso, String ubicacion) {
        if (cantidadActual > cantidadInicial) {
            throw new IllegalArgumentException("cantidadActual no puede superar cantidadInicial");
        }

        this.id = id;
        this.insumoId = insumoId;
        this.numeroLote = numeroLote;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaIngreso = fechaIngreso;
        this.ubicacion = ubicacion;
    }

    /**
     * Descompone el stock actual del lote en cajas cerradas + unidades sueltas,
     * usando el factor de conversión del insumo (1 caja = N unidades).
     *
     * Ej: 53 unidades con factor 10 → 5 cajas y 3 unidades sueltas.
     *
     * @param unidadesPorCaja factor del insumo (cuántas unidades tiene una caja)
     * @return un CajasUnidades(cajas, unidadesSueltas) listo para leer por nombre
     */
    public CajasUnidades calcularCajasDisponibles(int unidadesPorCaja) {
        return new CajasUnidades(
                cantidadActual / unidadesPorCaja,
                cantidadActual % unidadesPorCaja
        );
    }

    public EstadoSemaforo calcularEstadoSemaforo(ConfiguracionSemaforo conf, LocalDate hoy) {
        if (this.cantidadActual <= 0) {
            return EstadoSemaforo.AGOTADO;
        }
        Long diasDiferencia = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
        if (diasDiferencia > conf.getDiasVerde()) {
            return EstadoSemaforo.VERDE;
        } else if (diasDiferencia > conf.getDiasAmarillo()) {
            return EstadoSemaforo.AMARILLO;
        } else {
            return EstadoSemaforo.ROJO;
        }
    }

    public void registrarSalida(int cantidad) {
        if (!puedeDescontar(cantidad)) {
            throw new IllegalStateException("No se puede descontar la cantidad solicitada, " + "disponible: " + this.cantidadActual);
        } else {
            this.cantidadActual -= cantidad;
        }
    }

    public void registrarEntrada(int cantidad) {
        if (cantidad > 0) {
            this.cantidadActual += cantidad;
        } else {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }

    public boolean puedeDescontar(int cantidad) {
        return cantidad > 0 && cantidad <= cantidadActual;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}