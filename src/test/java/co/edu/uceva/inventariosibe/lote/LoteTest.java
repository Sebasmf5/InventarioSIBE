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

    @Test
    void deberiaCalcularCajasYUnidadesConResiduo() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        // 53 unidades con factor 10 → 5 cajas y 3 unidades sueltas
        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-001",
                vencimiento, 100, 53, hoy.minusDays(10), "Armario Gris");

        Lote.CajasUnidades resultado = lote.calcularCajasDisponibles(10);

        assertEquals(5, resultado.cajas());
        assertEquals(3, resultado.unidadesSueltas());
    }

    @Test
    void deberiaCalcularCajasYUnidadesSinResiduo() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        // 20 unidades con factor 10 → 2 cajas exactas, 0 sueltas
        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-002",
                vencimiento, 100, 20, hoy.minusDays(10), "Armario Gris");

        Lote.CajasUnidades resultado = lote.calcularCajasDisponibles(10);

        assertEquals(2, resultado.cajas());
        assertEquals(0, resultado.unidadesSueltas());
    }

    @Test
    void deberiaCalcularCeroCajasYUnidadesConStockCero() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003",
                vencimiento, 100, 0, hoy.minusDays(10), "Armario Gris");

        Lote.CajasUnidades resultado = lote.calcularCajasDisponibles(10);

        assertEquals(0, resultado.cajas());
        assertEquals(0, resultado.unidadesSueltas());
    }

    @Test
    void deberiaCalcularConFactorUnoTodoSonCajas() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        // Con factor 1, cada unidad es una "caja" (insumo sin empaque)
        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-004",
                vencimiento, 100, 5, hoy.minusDays(10), "Armario Gris");

        Lote.CajasUnidades resultado = lote.calcularCajasDisponibles(1);

        assertEquals(5, resultado.cajas());
        assertEquals(0, resultado.unidadesSueltas());
    }

    @Test
    void deberiaVerificarInvarianteCajasPorFactorMasSueltasEsTotal() {
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        // Invariante: cajas * unidadesPorCaja + unidadesSueltas == cantidadActual
        // Caso con residuo: 53 u, factor 10
        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-005",
                vencimiento, 100, 53, hoy.minusDays(10), "Armario Gris");
        int unidadesPorCaja = 10;

        Lote.CajasUnidades resultado = lote.calcularCajasDisponibles(unidadesPorCaja);

        int totalReconstruido = resultado.cajas() * unidadesPorCaja + resultado.unidadesSueltas();
        assertEquals(lote.getCantidadActual(), totalReconstruido);
    }
}