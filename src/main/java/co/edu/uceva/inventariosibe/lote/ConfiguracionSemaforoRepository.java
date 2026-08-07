package co.edu.uceva.inventariosibe.lote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfiguracionSemaforoRepository extends JpaRepository<ConfiguracionSemaforo, UUID> {
}