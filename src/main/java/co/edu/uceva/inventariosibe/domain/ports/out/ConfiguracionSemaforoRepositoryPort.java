package co.edu.uceva.inventariosibe.domain.ports.out;

import co.edu.uceva.inventariosibe.domain.model.ConfiguracionSemaforo;

public interface ConfiguracionSemaforoRepositoryPort {
    ConfiguracionSemaforo obtener();
    ConfiguracionSemaforo actualizar(ConfiguracionSemaforo configuracion);
}
