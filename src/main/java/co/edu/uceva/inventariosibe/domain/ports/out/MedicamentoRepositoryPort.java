package co.edu.uceva.inventariosibe.domain.ports.out;

import co.edu.uceva.inventariosibe.domain.model.Medicamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicamentoRepositoryPort {
    Medicamento guardar(Medicamento medicamento);
    Optional<Medicamento> buscarPorId(UUID id);
    List<Medicamento> buscarTodos();
}
