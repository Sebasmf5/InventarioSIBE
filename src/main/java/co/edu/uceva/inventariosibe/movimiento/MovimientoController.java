package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoRequestDTO;
import co.edu.uceva.inventariosibe.movimiento.dto.MovimientoResponseDTO;
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
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarMovimiento(@Valid @RequestBody MovimientoRequestDTO dto) {
        return movimientoService.registrarMovimiento(dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MovimientoResponseDTO> listarTodos() {
        return movimientoService.listarTodos();
    }

    @GetMapping("/por-lote/{loteId}")
    @PreAuthorize("isAuthenticated()")
    public List<MovimientoResponseDTO> listarPorLote(@PathVariable UUID loteId) {
        return movimientoService.listarPorLote(loteId);
    }

    @GetMapping("/por-usuario/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public List<MovimientoResponseDTO> listarPorUsuario(@PathVariable UUID usuarioId) {
        return movimientoService.listarMovimientosPorUsuario(usuarioId);
    }

}