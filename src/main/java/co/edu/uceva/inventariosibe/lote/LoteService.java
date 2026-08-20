package co.edu.uceva.inventariosibe.lote;

import co.edu.uceva.inventariosibe.insumo.Insumo;
import co.edu.uceva.inventariosibe.insumo.InsumoRepository;
import co.edu.uceva.inventariosibe.lote.dto.LoteResponseDTO;
import co.edu.uceva.inventariosibe.lote.dto.RegistrarLoteRequestDTO;
import co.edu.uceva.inventariosibe.movimiento.Movimiento;
import co.edu.uceva.inventariosibe.movimiento.MovimientoRepository;
import co.edu.uceva.inventariosibe.movimiento.TipoMovimiento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final InsumoRepository insumoRepository;
    private final MovimientoRepository movimientoRepository;
    private final ConfiguracionSemaforoRepository configuracionSemaforoRepository;

    public LoteService(LoteRepository loteRepository,
                       InsumoRepository insumoRepository,
                       MovimientoRepository movimientoRepository,
                       ConfiguracionSemaforoRepository configuracionSemaforoRepository) {
        this.loteRepository = loteRepository;
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
        this.configuracionSemaforoRepository = configuracionSemaforoRepository;
    }

    @Transactional
    public LoteResponseDTO registrarLote(RegistrarLoteRequestDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new NoSuchElementException("No se encontró el insumo con id " + dto.getInsumoId()));

        insumo.validarPuedeRecibirLote();

        Lote nuevoLote = new Lote(
                null,
                dto.getInsumoId(),
                dto.getNumeroLote(),
                dto.getFechaVencimiento(),
                dto.getCantidadInicial(),
                dto.getCantidadInicial(),
                LocalDate.now(),
                dto.getUbicacion()
        );
        Lote guardado = loteRepository.save(nuevoLote);

        Movimiento movimiento = new Movimiento(
                null,
                guardado.getId(),
                dto.getUsuarioId(),
                TipoMovimiento.ENTRADA,
                dto.getCantidadInicial(),
                LocalDateTime.now(),
                dto.getObservacion()
        );
        movimientoRepository.save(movimiento);

        return aResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listar() {
        return loteRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoteResponseDTO buscarPorId(UUID id) {
        return aResponse(buscarEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarPorInsumo(UUID insumoId) {
        return loteRepository.findByInsumoId(insumoId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public LoteResponseDTO cambiarEstadoLote(UUID id, boolean activo) {
        Lote lote = buscarEntidadPorId(id);
        if (lote.isActivo() == activo) {
            return aResponse(lote);
        }
        lote.setActivo(activo);
        return aResponse(loteRepository.save(lote));
    }

    private LoteResponseDTO aResponse(Lote lote) {
        LocalDate hoy = LocalDate.now();
        ConfiguracionSemaforo conf = configuracionSemaforoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe configuración de semáforo en la base de datos"));
        EstadoSemaforo estado = lote.calcularEstadoSemaforo(conf, hoy);
        Long diasRestantes = estado == EstadoSemaforo.AGOTADO
                ? null
                : ChronoUnit.DAYS.between(hoy, lote.getFechaVencimiento());
        return new LoteResponseDTO(lote, estado, diasRestantes);
    }

    private Lote buscarEntidadPorId(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el lote con id " + id));
    }
}