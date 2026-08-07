package co.edu.uceva.inventariosibe.lote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "configuracion_semaforo")
public class ConfiguracionSemaforo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dias_verde", nullable = false)
    private int diasVerde;

    @Column(name = "dias_amarillo", nullable = false)
    private int diasAmarillo;

    @Column(name = "dias_rojo", nullable = false)
    private int diasRojo;

    protected ConfiguracionSemaforo() {
    }

    public ConfiguracionSemaforo(UUID id, int diasVerde, int diasAmarillo, int diasRojo) {
        if (diasVerde < diasAmarillo || diasVerde < diasRojo || diasAmarillo < diasRojo) {
            throw new IllegalArgumentException("Dias Verdes de la semaforo incorrecta");
        }
        this.id = id;
        this.diasVerde = diasVerde;
        this.diasAmarillo = diasAmarillo;
        this.diasRojo = diasRojo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getDiasVerde() {
        return diasVerde;
    }

    public void setDiasVerde(int diasVerde) {
        this.diasVerde = diasVerde;
    }

    public int getDiasAmarillo() {
        return diasAmarillo;
    }

    public void setDiasAmarillo(int diasAmarillo) {
        this.diasAmarillo = diasAmarillo;
    }

    public int getDiasRojo() {
        return diasRojo;
    }

    public void setDiasRojo(int diasRojo) {
        this.diasRojo = diasRojo;
    }
}