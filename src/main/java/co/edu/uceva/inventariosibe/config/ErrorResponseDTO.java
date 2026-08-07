package co.edu.uceva.inventariosibe.config;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private Map<String, String> campos;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String mensaje, Map<String, String> campos) {
        this.timestamp = timestamp;
        this.status = status;
        this.mensaje = mensaje;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public void setCampos(Map<String, String> campos) {
        this.campos = campos;
    }
}