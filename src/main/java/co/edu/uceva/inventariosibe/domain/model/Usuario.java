package co.edu.uceva.inventariosibe.domain.model;

import java.util.UUID;

public class Usuario {
    private final UUID id;
    private String nombre;
    private final String email;
    private String passwordHash;
    private Rol rol;
    private boolean activo;

    public Usuario(UUID id, String nombre, String email, String passwordHash, Rol rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public boolean esSupervisor(){
        return rol==Rol.SUPERVISOR;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
