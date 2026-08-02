package co.edu.uceva.inventariosibe.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Lote {
    private final UUID id;
    private final UUID medicamentoId;
    private final String numeroLote;
    private final LocalDate fechaVencimiento;
    private final int cantidadInicial;
    private int cantidadActual; // El único campo que no es final
    private final LocalDate fechaIngreso;

    public Lote(UUID id, UUID medicamentoId, String numeroLote, LocalDate fechaVencimiento, int cantidadInicial, int cantidadActual, LocalDate fechaIngreso) {
        if (cantidadActual > cantidadInicial) {
            throw new IllegalArgumentException("cantidadActual no puede superar cantidadInicial");
        }

        this.id = id;
        this.medicamentoId = medicamentoId;
        this.numeroLote = numeroLote;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaIngreso = fechaIngreso;
    }

    public EstadoSemaforo calcularEstadoSemaforo(ConfiguracionSemaforo conf, LocalDate hoy) {
        if (this.cantidadActual <= 0) {
            return EstadoSemaforo.AGOTADO;
        }
        Long diasDiferencia = ChronoUnit.DAYS.between(hoy, fechaVencimiento);
        if (diasDiferencia > conf.getDiasVerdes()) {
            return EstadoSemaforo.VERDE;
        }else if (diasDiferencia > conf.getDiasAmarillo()) {
            return EstadoSemaforo.AMARILLO;
        }else{
            return EstadoSemaforo.ROJO;
        }
    }


    public void registrarSalida(int cantidad) {
        if (!puedeDescontar(cantidad)) {
            throw new IllegalStateException("No se puede descontar la cantidad solicitada, " + "disponible: " + this.cantidadActual);
        }else {
            this.cantidadActual -= cantidad;
        }
    }

    public void registrarEntrada(int cantidad) {
        if (cantidad > 0) {
            this.cantidadActual += cantidad;
        }else{
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
    }

    public boolean puedeDescontar(int cantidad) {
        return cantidad > 0 && cantidad <= cantidadActual;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMedicamentoId() {
        return medicamentoId;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidadInicial() {
        return cantidadInicial;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }


    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

}
