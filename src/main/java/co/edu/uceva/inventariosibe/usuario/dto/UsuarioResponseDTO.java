package co.edu.uceva.inventariosibe.usuario.dto;

import co.edu.uceva.inventariosibe.usuario.Rol;
import co.edu.uceva.inventariosibe.usuario.Usuario;

import java.util.UUID;

public class UsuarioResponseDTO {

    private UUID id;
    private String nombre;
    private String email;
    private Rol rol;
    private boolean activo;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}