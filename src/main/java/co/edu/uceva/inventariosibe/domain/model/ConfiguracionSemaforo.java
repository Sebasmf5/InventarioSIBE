package co.edu.uceva.inventariosibe.domain.model;

import java.util.UUID;

public class ConfiguracionSemaforo {
    private UUID id;
    private int diasVerdes;
    private int diasAmarillo;
    private int diasRojos;

    public ConfiguracionSemaforo(UUID id, int diasVerdes, int diasAmarillo, int diasRojos) {
        if(diasVerdes < diasAmarillo || diasVerdes < diasRojos || diasAmarillo < diasRojos) {
            throw new IllegalArgumentException("Dias Verdes de la semaforo incorrecta");
        } else {
            this.id = id;
            this.diasVerdes = diasVerdes;
            this.diasAmarillo = diasAmarillo;
            this.diasRojos = diasRojos;
        }
    }

    public int getDiasRojos() {
        return diasRojos;
    }

    public UUID getId() {
        return id;
    }

    public void setDiasRojos(int diasRojos) {
        this.diasRojos = diasRojos;
    }

    public int getDiasAmarillo() {
        return diasAmarillo;
    }

    public void setDiasAmarillo(int diasAmarillo) {
        this.diasAmarillo = diasAmarillo;
    }

    public int getDiasVerdes() {
        return diasVerdes;
    }

    public void setDiasVerdes(int diasVerdes) {
        this.diasVerdes = diasVerdes;
    }
}
