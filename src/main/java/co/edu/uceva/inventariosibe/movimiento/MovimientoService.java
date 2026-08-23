package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.insumo.Insumo;
import co.edu.uceva.inventariosibe.insumo.InsumoRepository;
import co.edu.uceva.inventariosibe.lote.Lote;
import co.edu.uceva.inventariosibe.lote.LoteRepository;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoRequestDTO;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoResponseDTO;
import co.edu.uceva.inventariosibe.usuario.Usuario;
import co.edu.uceva.inventariosibe.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final InsumoRepository insumoRepository;

    public MovimientoService(LoteRepository loteRepository,
                             MovimientoRepository movimientoRepository,
                             UsuarioRepository usuarioRepository,
                             InsumoRepository insumoRepository) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.insumoRepository = insumoRepository;
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
        return aResponse(guardado);
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
    public List<MovimientoResponseDTO> listarTodos() {
        return movimientoRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarPorLote(UUID loteId) {
        return movimientoRepository.findByLoteId(loteId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarMovimientosPorUsuario(UUID usuarioId) {
        return movimientoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::aResponse)
                .toList();
    }

    /**
     * Construye el ResponseDTO enriquecido con datos de usuario, lote e insumo
     * para que el frontend tenga todo lo necesario para auditoría sin hacer
     * llamadas extra.
     */
    private MovimientoResponseDTO aResponse(Movimiento movimiento) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO(movimiento);

        // Resolver nombre del usuario que hizo el movimiento
        String nombreUsuario = usuarioRepository.findById(movimiento.getUsuarioId())
                .map(Usuario::getNombre)
                .orElse("Usuario eliminado");
        dto.setNombreUsuario(nombreUsuario);

        // Resolver numero de lote e insumo (para el panel general)
        Lote lote = loteRepository.findById(movimiento.getLoteId()).orElse(null);
        if (lote != null) {
            dto.setNumeroLote(lote.getNumeroLote());
            int unidadesPorCaja = insumoRepository.findById(lote.getInsumoId())
                    .map(Insumo::getUnidadesPorCaja)
                    .orElse(1);
            dto.setUnidadesPorCaja(unidadesPorCaja);
            dto.setCajas(movimiento.getCantidad() / unidadesPorCaja);
            dto.setUnidadesSueltas(movimiento.getCantidad() % unidadesPorCaja);
            dto.setCantidadFormateada(dto.getCajas() + " cajas y " + dto.getUnidadesSueltas() + " unidades");
            String nombreInsumo = insumoRepository.findById(lote.getInsumoId())
                    .map(Insumo::getNombre)
                    .orElse("Insumo eliminado");
            dto.setNombreInsumo(nombreInsumo);
        } else {
            dto.setNumeroLote("Lote eliminado");
            dto.setNombreInsumo("Insumo eliminado");
            dto.setUnidadesPorCaja(1);
            dto.setCajas(movimiento.getCantidad());
            dto.setUnidadesSueltas(0);
            dto.setCantidadFormateada(movimiento.getCantidad() + " cajas y 0 unidades");
        }

        return dto;
    }
}