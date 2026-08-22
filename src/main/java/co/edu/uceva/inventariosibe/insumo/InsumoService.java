package co.edu.uceva.inventariosibe.insumo;

import co.edu.uceva.inventariosibe.insumo.dto.InsumoRequestDTO;
import co.edu.uceva.inventariosibe.insumo.dto.InsumoResponseDTO;
import co.edu.uceva.inventariosibe.lote.ConfiguracionSemaforo;
import co.edu.uceva.inventariosibe.lote.ConfiguracionSemaforoRepository;
import co.edu.uceva.inventariosibe.lote.EstadoSemaforo;
import co.edu.uceva.inventariosibe.lote.Lote;
import co.edu.uceva.inventariosibe.lote.LoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final LoteRepository loteRepository;
    private final ConfiguracionSemaforoRepository configuracionSemaforoRepository;

    public InsumoService(InsumoRepository insumoRepository,
                         LoteRepository loteRepository,
                         ConfiguracionSemaforoRepository configuracionSemaforoRepository) {
        this.insumoRepository = insumoRepository;
        this.loteRepository = loteRepository;
        this.configuracionSemaforoRepository = configuracionSemaforoRepository;
    }

    @Transactional
    public InsumoResponseDTO crear(InsumoRequestDTO dto) {
        Insumo insumo = new Insumo(
                null,
                dto.getNombre(),
                dto.getPresentacion(),
                dto.getUnidadMedida(),
                dto.getStockMinimo(),
                dto.getActivo(),
                dto.getMarca(),
                dto.getTipo(),
                dto.getRegistroInvima(),
                dto.getUnidadesPorCaja()
        );
        return aResponse(insumoRepository.save(insumo));
    }

    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar() {
        return insumoRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InsumoResponseDTO buscarPorId(UUID id) {
        return aResponse(buscarEntidadPorId(id));
    }

    @Transactional
    public InsumoResponseDTO actualizar(UUID id, InsumoRequestDTO dto) {
        Insumo insumo = buscarEntidadPorId(id);
        insumo.setNombre(dto.getNombre());
        insumo.setPresentacion(dto.getPresentacion());
        insumo.setUnidadMedida(dto.getUnidadMedida());
        insumo.setStockMinimo(dto.getStockMinimo());
        insumo.setActivo(dto.getActivo());
        insumo.setMarca(dto.getMarca());
        insumo.setTipo(dto.getTipo());
        insumo.setRegistroInvima(dto.getRegistroInvima());
        insumo.setUnidadesPorCaja(dto.getUnidadesPorCaja());
        return aResponse(insumoRepository.save(insumo));
    }

    @Transactional
    public void desactivar(UUID id) {
        Insumo insumo = buscarEntidadPorId(id);
        insumo.setActivo(false);
        insumoRepository.save(insumo);
    }

    private Insumo buscarEntidadPorId(UUID id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el insumo con id " + id));
    }

    /**
     * Construye el ResponseDTO calculando el estado agregado del insumo:
     * el PEOR estado (mayor severidad) entre sus lotes activos.
     * Devuelve null si el insumo no tiene lotes activos (el front lo
     * muestra como "Sin lotes").
     */
    private InsumoResponseDTO aResponse(Insumo insumo) {
        LocalDate hoy = LocalDate.now();
        ConfiguracionSemaforo conf = configuracionSemaforoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe configuración de semáforo en la base de datos"));

        EstadoSemaforo peor = null;
        int peorSev = -1;
        for (Lote lote : loteRepository.findByInsumoId(insumo.getId())) {
            if (!lote.isActivo()) continue;
            EstadoSemaforo estado = lote.calcularEstadoSemaforo(conf, hoy);
            if (estado.getSeveridad() > peorSev) {
                peor = estado;
                peorSev = estado.getSeveridad();
            }
        }
        return new InsumoResponseDTO(insumo, peor);
    }
}