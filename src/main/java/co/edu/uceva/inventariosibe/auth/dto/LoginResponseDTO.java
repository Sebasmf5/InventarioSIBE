package co.edu.uceva.inventariosibe.auth.dto;

import co.edu.uceva.inventariosibe.usuario.Rol;

public class LoginResponseDTO {

    private String token;
    private String email;
    private String nombre;
    private Rol rol;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String email, String nombre, Rol rol) {
        this.token = token;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}