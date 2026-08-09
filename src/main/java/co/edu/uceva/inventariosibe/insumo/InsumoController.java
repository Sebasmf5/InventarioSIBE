package co.edu.uceva.inventariosibe.insumo;

import co.edu.uceva.inventariosibe.insumo.dto.InsumoRequestDTO;
import co.edu.uceva.inventariosibe.insumo.dto.InsumoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<InsumoResponseDTO> listar() {
        return insumoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public InsumoResponseDTO buscarPorId(@PathVariable UUID id) {
        return insumoService.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public InsumoResponseDTO crear(@Valid @RequestBody InsumoRequestDTO dto) {
        return insumoService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public InsumoResponseDTO actualizar(@PathVariable UUID id, @Valid @RequestBody InsumoRequestDTO dto) {
        return insumoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        insumoService.desactivar(id);
    }
}