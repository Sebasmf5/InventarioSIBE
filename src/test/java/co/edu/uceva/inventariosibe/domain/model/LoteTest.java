package co.edu.uceva.inventariosibe.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoteTest {

    @Test
    void deberiaSerVerdeSiFaltanMasDe90Dias() {
        // Arrange: preparo los datos que necesito
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(90); // vence en 120 días, más de 90

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-001",
                vencimiento, 100, 50, hoy.minusDays(10));

        // Act: ejecuto el método que quiero probar
        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        // Assert: verifico que el resultado sea el esperado
        assertEquals(EstadoSemaforo.AMARILLO, resultado);
    }

    @Test
    void deberiaSerAmarilloSiFaltanEntre31y89Dias() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(35);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-002", vencimiento, 100, 50, hoy.minusDays(10));

        // act: Ejecuto el método que quiero probar
        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        //Assert: verificar que el resutlado sea el correcto
        assertEquals(EstadoSemaforo.AMARILLO, resultado);
    }

    @Test
    void deberiaSerRojoSiFaltan30Dias() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(30);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003", vencimiento, 100, 50, hoy.minusDays(10));

        // act: Ejecuto el método que quiero probar
        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        //Assert: verificar que el resutlado sea el correcto
        assertEquals(EstadoSemaforo.ROJO, resultado);
    }

    @Test
    void deberiaSerRojoSiVencio() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.minusDays(12);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003", vencimiento, 100, 50, hoy.minusDays(10));

        // act: Ejecuto el método que quiero probar
        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        //Assert: verificar que el resutlado sea el correcto
        assertEquals(EstadoSemaforo.ROJO, resultado);
    }

    @Test
    void deberiaSerAgotadoCantidadActualCero() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003", vencimiento, 100, 0, hoy.minusDays(10));

        // act: Ejecuto el método que quiero probar
        EstadoSemaforo resultado = lote.calcularEstadoSemaforo(config, hoy);

        //Assert: verificar que el resutlado sea el correcto
        assertEquals(EstadoSemaforo.AGOTADO, resultado);
    }

    @Test
    void ilegalStateExceptionPorCantidadMayorQueLaDisponible() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003", vencimiento, 100, 80, hoy.minusDays(10));

        assertThrows(IllegalStateException.class, () -> lote.registrarSalida(99));
    }

    @Test
    void descuentoValido() {
        //Arrange: preparar los datos
        ConfiguracionSemaforo config = new ConfiguracionSemaforo(UUID.randomUUID(), 90, 30, 0);
        LocalDate hoy = LocalDate.of(2026, 8, 1);
        LocalDate vencimiento = hoy.plusDays(200);

        Lote lote = new Lote(UUID.randomUUID(), UUID.randomUUID(), "L-003", vencimiento, 100, 80, hoy.minusDays(10));
        lote.registrarSalida(70);
        assertEquals(10, lote.getCantidadActual());
    }
}
