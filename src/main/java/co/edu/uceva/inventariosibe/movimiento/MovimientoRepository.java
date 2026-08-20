package co.edu.uceva.inventariosibe.movimiento;

import co.edu.uceva.inventariosibe.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoRepository extends JpaRepository<Movimiento, UUID> {
    List<Movimiento> findByLoteId(UUID loteId);
    List<Movimiento> findByUsuarioId(UUID usuarioId);
}