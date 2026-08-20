package co.edu.uceva.inventariosibe.auth;

import co.edu.uceva.inventariosibe.auth.dto.LoginRequestDTO;
import co.edu.uceva.inventariosibe.auth.dto.LoginResponseDTO;
import co.edu.uceva.inventariosibe.config.JwtService;
import co.edu.uceva.inventariosibe.usuario.Usuario;
import co.edu.uceva.inventariosibe.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            throw new IllegalStateException("El usuario está inactivo");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());

        return new LoginResponseDTO(usuario.getId(), token, usuario.getEmail(), usuario.getNombre(), usuario.getRol());
    }
}