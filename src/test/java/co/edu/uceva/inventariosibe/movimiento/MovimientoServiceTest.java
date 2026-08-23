package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.insumo.Insumo;
import co.edu.uceva.inventariosibe.insumo.InsumoRepository;
import co.edu.uceva.inventariosibe.insumo.TipoInsumo;
import co.edu.uceva.inventariosibe.lote.Lote;
import co.edu.uceva.inventariosibe.lote.LoteRepository;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoResponseDTO;
import co.edu.uceva.inventariosibe.usuario.Usuario;
import co.edu.uceva.inventariosibe.usuario.UsuarioRepository;
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
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);

        UUID loteId = UUID.randomUUID();
        UUID insumoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Lote lote = new Lote(loteId, insumoId, "L-001",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5), "Armario Gris");
        Insumo insumo = new Insumo(insumoId, "Acetaminofén", "Tableta",
                "100mg", 50, true, "Genfar", TipoInsumo.MEDICAMENTO, "INVIMA-123", 10);
        Usuario usuario = new Usuario(usuarioId, "Enfermera Ana", "ana@hospital.com", "hash", co.edu.uceva.inventariosibe.usuario.Rol.ENFERMERIA, true);

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository, usuarioRepository, insumoRepository);

        MovimientoResponseDTO response = service.registrarMovimiento(loteId, usuarioId, TipoMovimiento.SALIDA, 5, "Administrado a paciente");

        assertEquals(15, lote.getCantidadActual());
        assertNotNull(response);
        assertEquals(loteId, response.getLoteId());
        assertEquals(5, response.getCantidad());
        assertEquals("Enfermera Ana", response.getNombreUsuario());

        verify(loteRepository, times(1)).save(lote);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void deberiaLanzarExcepcionSiElLoteNoExiste() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);

        UUID loteId = UUID.randomUUID();
        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository, usuarioRepository, insumoRepository);

        assertThrows(NoSuchElementException.class, () ->
                service.registrarMovimiento(loteId, UUID.randomUUID(), TipoMovimiento.SALIDA, 5, "observacion")
        );
    }

    @Test
    void deberiaRegistrarEntradaYActualizarElLote() {
        LoteRepository loteRepository = mock(LoteRepository.class);
        MovimientoRepository movimientoRepository = mock(MovimientoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        InsumoRepository insumoRepository = mock(InsumoRepository.class);

        UUID loteId = UUID.randomUUID();
        UUID insumoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Lote lote = new Lote(loteId, insumoId, "L-002",
                LocalDate.now().plusDays(120), 100, 20, LocalDate.now().minusDays(5), "Armario Gris");
        Insumo insumo = new Insumo(insumoId, "Ibuprofeno", "Jarabe",
                "100mg/ml", 30, true, "MK", TipoInsumo.MEDICAMENTO, "INVIMA-456", 5);
        Usuario usuario = new Usuario(usuarioId, "Supervisor Juan", "juan@hospital.com", "hash", co.edu.uceva.inventariosibe.usuario.Rol.SUPERVISOR, true);

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));

        MovimientoService service = new MovimientoService(loteRepository, movimientoRepository, usuarioRepository, insumoRepository);

        MovimientoResponseDTO response = service.registrarMovimiento(loteId, usuarioId, TipoMovimiento.ENTRADA, 10, "Reposicion de stock");

        assertEquals(30, lote.getCantidadActual());
        assertNotNull(response);
        assertEquals(loteId, response.getLoteId());
        assertEquals(10, response.getCantidad());
        assertEquals("Supervisor Juan", response.getNombreUsuario());
        verify(loteRepository, times(1)).save(lote);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }
}