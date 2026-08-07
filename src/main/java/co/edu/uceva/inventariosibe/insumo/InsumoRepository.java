package co.edu.uceva.inventariosibe.insumo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InsumoRepository extends JpaRepository<Insumo, UUID> {
}