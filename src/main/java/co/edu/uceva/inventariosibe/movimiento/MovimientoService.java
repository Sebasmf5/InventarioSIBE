package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.lote.Lote;
import co.edu.uceva.inventariosibe.lote.LoteRepository;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoRequestDTO;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class MovimientoService {

    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;

    public MovimientoService(LoteRepository loteRepository, MovimientoRepository movimientoRepository) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public MovimientoResponseDTO registrarMovimiento(UUID loteId, UUID usuarioId, TipoMovimiento tipo,
                                      int cantidad, String observacion) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el lote con id " + loteId));

        if (tipo == TipoMovimiento.ENTRADA) {
            lote.registrarEntrada(cantidad);
        } else {
            lote.registrarSalida(cantidad);
        }
        loteRepository.save(lote);

        Movimiento movimiento = new Movimiento(
                null,
                loteId,
                usuarioId,
                tipo,
                cantidad,
                LocalDateTime.now(),
                observacion
        );
        Movimiento guardado = movimientoRepository.save(movimiento);
        return new MovimientoResponseDTO(guardado);
    }

    @Transactional
    public MovimientoResponseDTO registrarMovimiento(MovimientoRequestDTO dto) {
        return registrarMovimiento(
                dto.getLoteId(),
                dto.getUsuarioId(),
                dto.getTipo(),
                dto.getCantidad(),
                dto.getObservacion()
        );
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarPorLote(UUID loteId) {
        return movimientoRepository.findByLoteId(loteId).stream()
                .map(MovimientoResponseDTO::new)
                .toList();
    }
}