package co.edu.uceva.inventariosibe.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import co.edu.uceva.inventariosibe.domain.model.*;
import co.edu.uceva.inventariosibe.domain.ports.out.LoteRepositoryPort;
import co.edu.uceva.inventariosibe.domain.ports.out.MovimientoRepositoryPort;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrarMovimientoUseCaseTest {

    @Test
    void deberiaRegistrarSalidaYActualizarElLote() {
        // Arrange: creo los mocks (los "actores falsos") de ambos puertos
        LoteRepositoryPort loteRepositoryPort = mock(LoteRepositoryPort.class);
        MovimientoRepositoryPort movimientoRepositoryPort = mock(MovimientoRepositoryPort.class);

        UUID loteId = UUID.randomUUID();
        Lote lote = new Lote(loteId, UUID.randomUUID(), "L-001",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5));

        // Le doy el "guion" al mock: "cuando te pregunten por este loteId, responde con este lote"
        when(loteRepositoryPort.buscarPorId(loteId)).thenReturn(Optional.of(lote));

        RegistrarMovimientoUseCase useCase =
                new RegistrarMovimientoUseCase(loteRepositoryPort, movimientoRepositoryPort);

        // Act: ejecuto el caso de uso real (esta clase SÍ es real, no un mock)
        useCase.ejecutar(loteId, UUID.randomUUID(), TipoMovimiento.SALIDA, 5, "Administrado a paciente");

        // Assert: verifico que el lote quedó con la cantidad correcta
        assertEquals(15, lote.getCantidadActual());

        // Assert adicional: verifico que efectivamente se haya LLAMADO a guardar,
        // una vez cada uno (esto es específico de Mockito, no de JUnit)
        verify(loteRepositoryPort, times(1)).guardar(lote);
        verify(movimientoRepositoryPort, times(1)).guardar(any(Movimiento.class));
    }

    @Test
    void deberiaLanzarExcepcionSiElLoteNoExiste() {
        // Arrange: creo los mocks
        LoteRepositoryPort loteRepositoryPort = mock(LoteRepositoryPort.class);
        MovimientoRepositoryPort movimientoRepositoryPort = mock(MovimientoRepositoryPort.class);

        UUID loteId = UUID.randomUUID();

        //cuando pregunten por este loteId, responde  que no existe"
        when(loteRepositoryPort.buscarPorId(loteId)).thenReturn(Optional.empty());

        RegistrarMovimientoUseCase useCase =
                new RegistrarMovimientoUseCase(loteRepositoryPort, movimientoRepositoryPort);

        // Act + Assert: verifico que ejecutar() lance la excepción esperada
        assertThrows(NoSuchElementException.class, () ->
                useCase.ejecutar(loteId, UUID.randomUUID(), TipoMovimiento.SALIDA, 5, "observacion")
        );
    }

    @Test
    void deberiaRegistrarEntradaYActualizarElLote() {
        LoteRepositoryPort loteRepositoryPort = mock(LoteRepositoryPort.class);
        MovimientoRepositoryPort movimientoRepositoryPort = mock(MovimientoRepositoryPort.class);

        UUID loteId = UUID.randomUUID();
        Lote lote = new Lote(loteId, UUID.randomUUID(), "L-002",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5));

        when(loteRepositoryPort.buscarPorId(loteId)).thenReturn(Optional.of(lote));

        RegistrarMovimientoUseCase useCase =
                new RegistrarMovimientoUseCase(loteRepositoryPort, movimientoRepositoryPort);

        useCase.ejecutar(loteId, UUID.randomUUID(), TipoMovimiento.ENTRADA, 10, "Reposicion de stock");

        assertEquals(30, lote.getCantidadActual()); // 20 + 10
        verify(loteRepositoryPort, times(1)).guardar(lote);
        verify(movimientoRepositoryPort, times(1)).guardar(any(Movimiento.class));
    }
}