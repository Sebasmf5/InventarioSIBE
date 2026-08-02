package co.edu.uceva.inventariosibe.domain.ports.out;

import co.edu.uceva.inventariosibe.domain.model.Movimiento;

import java.util.List;
import java.util.UUID;

public interface MovimientoRepositoryPort {
    Movimiento guardar(Movimiento movimiento);
    List<Movimiento> buscarPorLote(UUID loteId);
}