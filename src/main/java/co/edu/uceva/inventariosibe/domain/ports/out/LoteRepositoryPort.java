package co.edu.uceva.inventariosibe.domain.ports.out;

import co.edu.uceva.inventariosibe.domain.model.Lote;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepositoryPort {
    Lote guardar(Lote lote);
    Optional<Lote> buscarPorId(UUID id);
    List<Lote> buscarTodos();
    List<Lote> buscarPorMedicamento(UUID medicamentoId);
}
