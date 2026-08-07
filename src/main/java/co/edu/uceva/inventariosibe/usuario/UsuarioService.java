package co.edu.uceva.inventariosibe.usuario;

import co.edu.uceva.inventariosibe.usuario.dto.UsuarioRequestDTO;
import co.edu.uceva.inventariosibe.usuario.dto.UsuarioResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario(
                null,
                dto.getNombre(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getRol(),
                dto.getActivo()
        );
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id) {
        return new UsuarioResponseDTO(buscarEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    private Usuario buscarEntidadPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el usuario con id " + id));
    }
}