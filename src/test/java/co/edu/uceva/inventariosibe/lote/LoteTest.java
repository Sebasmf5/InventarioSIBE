package co.edu.uceva.inventariosibe.lote;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoteTest {

    @Test
    void deberiaSerVerdeSiFaltanMasDe90Dias() {
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(120);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-001",
                vencimiento, 100, 50, hoy.minusDays(10), "Armario Gris");

        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        assertEquals(EstadoSemaforo.VERDE, resultado);
    }

    @Test
    void deberiaSerAmarilloSiFaltanEntre31y89Dias() {
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(35);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-002",
                vencimiento, 100, 50, hoy.minusDays(10), "Armario Gris");

        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        assertEquals(EstadoSemaforo.AMARILLO, resultado);
    }

    @Test
    void deberiaSerRojoSiFaltan30Dias() {
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(30);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 50, hoy.minusDays(10), "Armario Gris");

        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        assertEquals(EstadoSemaforo.ROJO, resultado);
    }

    @Test
    void deberiaSerRojoSiVencio() {
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.minusDays(12);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 50, hoy.minusDays(10), "Armario Gris");

        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        assertEquals(EstadoSemaforo.ROJO, resultado);
    }

    @Test
    void deberiaSerAgotadoCantidadActualCero() {
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 0, hoy.minusDays(10), "Armario Gris");

        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        assertEquals(EstadoSemaforo.AGOTADO, resultado);
    }

    @Test
    void ilegalStateExceptionPorCantidadMayorQueLaDisponible() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 80, hoy.minusDays(10), "Armario Gris");

        assertThrows(IllegalStateException.class, () -> lote.registrarSalida(99));
    }

    @Test
    void descuentoValido() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 80, hoy.minusDays(10), "Armario Gris");
        lote.registrarSalida(70);
        assertEquals(10, lote.getCantidadActual());
    }
}