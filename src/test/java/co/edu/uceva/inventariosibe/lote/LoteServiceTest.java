package co.edu.uceva.inventariosibe.lote;

import co.edu.uceva.inventariosibe.insumo.Insumo;
import co.edu.uceva.inventariosibe.insumo.InsumoRepository;
import co.edu.uceva.inventariosibe.insumo.TipoInsumo;
import co.edu.uceva.inventariosibe.lote.dto.RegistrarLoteRequestDTO;
import co.edu.uceva.inventariosibe.movimiento.MovimientoRepository;
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

class LoteServiceTest {

    @Test
    void deberiaRegistrarElLoteCorrectamente() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);

        UUID insumoId = UUID.randomUUID();
        Insumo insumo = new Insumo(insumoId, "Acetaminofén", "Tableta",
                "100mg", 50, true, "Genfar", TipoInsumo.MEDICAMENTO, "INVIMA-123");
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarLoteRequestDTO dto = new RegistrarLoteRequestDTO();
        dto.setInsumoId(insumoId);
        dto.setUsuarioId(UUID.randomUUID());
        dto.setNumeroLote("L-001");
        dto.setFechaVencimiento(LocalDate.now().plusDays(120));
        dto.setCantidadInicial(100);
        dto.setObservacion("Ingreso inicial");
        dto.setUbicacion("Armario Gris");

        LoteService service = new LoteService(loteRepository, insumoRepository, movimientoRepository);

        var response = service.registrarLote(dto);

        assertNotNull(response);
        assertEquals(insumoId, response.getInsumoId());
        assertEquals(100, response.getCantidadActual());
        verify(loteRepository, times(1)).save(any(Lote.class));
        verify(movimientoRepository, times(1)).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiElInsumoNoExiste() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);

        UUID insumoId = UUID.randomUUID();
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.empty());

        RegistrarLoteRequestDTO dto = new RegistrarLoteRequestDTO();
        dto.setInsumoId(insumoId);
        dto.setUsuarioId(UUID.randomUUID());
        dto.setNumeroLote("L-001");
        dto.setFechaVencimiento(LocalDate.now().plusDays(120));
        dto.setCantidadInicial(100);
        dto.setUbicacion("Armario Gris");

        LoteService service = new LoteService(loteRepository, insumoRepository, movimientoRepository);

        assertThrows(NoSuchElementException.class, () -> service.registrarLote(dto));
        verify(loteRepository, never()).save(any());
    }
}