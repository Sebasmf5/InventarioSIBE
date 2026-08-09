package co.edu.uceva.inventariosibe.lote;

import co.edu.uceva.inventariosibe.lote.dto.LoteResponseDTO;
import co.edu.uceva.inventariosibe.lote.dto.RegistrarLoteRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteResponseDTO registrarLote(@Valid @RequestBody RegistrarLoteRequestDTO dto) {
        return loteService.registrarLote(dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<LoteResponseDTO> listar() {
        return loteService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public LoteResponseDTO buscarPorId(@PathVariable UUID id) {
        return loteService.buscarPorId(id);
    }

    @GetMapping("/por-insumo/{insumoId}")
    @PreAuthorize("isAuthenticated()")
    public List<LoteResponseDTO> listarPorInsumo(@PathVariable UUID insumoId) {
        return loteService.listarPorInsumo(insumoId);
    }
}