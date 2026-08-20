package co.edu.uceva.inventariosibe.lote;

public enum EstadoSemaforo {
    VERDE,
    AMARILLO,
    ROJO,
    AGOTADO;

    /**
     * Nivel de urgencia para comparar cuál estado es el "peor" al agregar
     * varios lotes (p. ej. el estado de un insumo = peor estado de sus lotes).
     * Mayor valor = más urgente.
     */
    public int getSeveridad() {
        return switch (this) {
            case ROJO -> 3;
            case AMARILLO -> 2;
            case VERDE -> 1;
            case AGOTADO -> 0;
        };
    }
}