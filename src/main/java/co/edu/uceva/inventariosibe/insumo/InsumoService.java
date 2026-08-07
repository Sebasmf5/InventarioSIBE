package co.edu.uceva.inventariosibe.insumo;

import co.edu.uceva.inventariosibe.insumo.dto.InsumoRequestDTO;
import co.edu.uceva.inventariosibe.insumo.dto.InsumoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class InsumoService {

    private final InsumoRepository insumoRepository;

    public InsumoService(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
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
                dto.getRegistroInvima()
        );
        return new InsumoResponseDTO(insumoRepository.save(insumo));
    }

    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar() {
        return insumoRepository.findAll().stream()
                .map(InsumoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public InsumoResponseDTO buscarPorId(UUID id) {
        return new InsumoResponseDTO(buscarEntidadPorId(id));
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
        return new InsumoResponseDTO(insumoRepository.save(insumo));
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
}