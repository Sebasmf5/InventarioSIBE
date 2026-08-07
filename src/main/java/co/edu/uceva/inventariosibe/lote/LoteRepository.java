package co.edu.uceva.inventariosibe.lote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {
    List<Lote> findByInsumoId(UUID insumoId);
}