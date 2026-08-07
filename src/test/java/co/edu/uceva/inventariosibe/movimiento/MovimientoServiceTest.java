package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.lote.Lote;
import co.edu.uceva.inventariosibe.lote.LoteRepository;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MovimientoServiceTest {

    @Test
    void deberiaRegistrarSalidaYActualizarElLote() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);

        UUID loteId = UUID.randomUUID();
        Lote lote = new Lote(loteId, UUID.randomUUID(), "L-001",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5), "Armario Gris");

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository);

        MovimientoResponseDTO response = service.registrarMovimiento(loteId, UUID.randomUUID(), TipoMovimiento.SALIDA, 5, "Administrado a paciente");

        assertEquals(15, lote.getCantidadActual());
        assertNotNull(response);
        assertEquals(loteId, response.getLoteId());
        assertEquals(5, response.getCantidad());

        verify(loteRepository, times(1)).save(lote);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void deberiaLanzarExcepcionSiElLoteNoExiste() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);

        UUID loteId = UUID.randomUUID();
        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository);

        assertThrows(NoSuchElementException.class, () ->
                service.registrarMovimiento(loteId, UUID.randomUUID(), TipoMovimiento.SALIDA, 5, "observacion")
        );
    }

    @Test
    void deberiaRegistrarEntradaYActualizarElLote() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);

        UUID loteId = UUID.randomUUID();
        Lote lote = new Lote(loteId, UUID.randomUUID(), "L-002",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5), "Armario Gris");

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository);

        MovimientoResponseDTO response = service.registrarMovimiento(loteId, UUID.randomUUID(), TipoMovimiento.ENTRADA, 10, "Reposicion de stock");

        assertEquals(30, lote.getCantidadActual());
        assertNotNull(response);
        assertEquals(loteId, response.getLoteId());
        assertEquals(10, response.getCantidad());
        verify(loteRepository, times(1)).save(lote);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }
}