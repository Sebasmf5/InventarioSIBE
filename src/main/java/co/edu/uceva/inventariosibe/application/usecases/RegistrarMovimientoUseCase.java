package co.edu.uceva.inventariosibe.application.usecases;

import co.edu.uceva.inventariosibe.domain.model.Lote;
import co.edu.uceva.inventariosibe.domain.model.Movimiento;
import co.edu.uceva.inventariosibe.domain.model.TipoMovimiento;
import co.edu.uceva.inventariosibe.domain.ports.out.LoteRepositoryPort;
import co.edu.uceva.inventariosibe.domain.ports.out.MovimientoRepositoryPort;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

public class RegistrarMovimientoUseCase {

    private final LoteRepositoryPort loteRepositoryPort;
    private final MovimientoRepositoryPort movimientoRepositoryPort;

    public RegistrarMovimientoUseCase(LoteRepositoryPort loteRepository, MovimientoRepositoryPort movimientoRepository) {
        this.loteRepositoryPort = loteRepository;
        this.movimientoRepositoryPort = movimientoRepository;
    }

    public void ejecutar(UUID loteId, UUID usuarioId, TipoMovimiento tipoMovimiento, int cantidad, String descripcion) {
        Lote lote = loteRepositoryPort.buscarPorId(loteId).
                orElseThrow(() -> new NoSuchElementException("No se encontró el lote con id " + loteId));
        if (tipoMovimiento == TipoMovimiento.ENTRADA) {
            lote.registrarEntrada(cantidad);
        }else {
            lote.registrarSalida(cantidad);
        }
        loteRepositoryPort.guardar(lote);
        //registrar el movimiento
        Movimiento movimiento = new Movimiento(UUID.randomUUID(), loteId, usuarioId, tipoMovimiento, cantidad, LocalDateTime.now(), descripcion);
        movimientoRepositoryPort.guardar(movimiento);
    }
}
