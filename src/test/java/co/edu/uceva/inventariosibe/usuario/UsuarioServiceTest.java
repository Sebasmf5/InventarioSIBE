package co.edu.uceva.inventariosibe.usuario;

import co.edu.uceva.inventariosibe.usuario.dto.UsuarioRequestDTO;
import co.edu.uceva.inventariosibe.usuario.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    @Test
    void crear_deberiaHashearPasswordYRetornarDTOSinPasswordHash() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(passwordEncoder.encode("plainPassword123")).thenReturn("hashed-pwd");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre("Ana Enfermera");
        dto.setEmail("ana@hospital.gov");
        dto.setPassword("plainPassword123");
        dto.setRol(Rol.ENFERMERIA);
        dto.setActivo(true);

        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder);
        UsuarioResponseDTO response = service.crear(dto);

        assertNotNull(response.getId());
        assertEquals("Ana Enfermera", response.getNombre());
        assertEquals("ana@hospital.gov", response.getEmail());
        assertEquals(Rol.ENFERMERIA, response.getRol());
        assertTrue(response.isActivo());

        verify(passwordEncoder).encode("plainPassword123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void buscarPorId_deberiaRetornarDTOCuandoExiste() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Ana", "ana@hospital.gov", "hash", Rol.ENFERMERIA, true);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder);
        UsuarioResponseDTO response = service.buscarPorId(id);

        assertEquals(id, response.getId());
        assertEquals("Ana", response.getNombre());
        assertEquals(Rol.ENFERMERIA, response.getRol());
    }

    @Test
    void buscarPorId_deberiaLanzarNoSuchElementCuandoNoExiste() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder);
        assertThrows(NoSuchElementException.class, () -> service.buscarPorId(id));
    }

    @Test
    void listar_deberiaRetornarListaDeDTOs() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        List<Usuario> usuarios = List.of(
                new Usuario(UUID.randomUUID(), "Ana", "ana@hospital.gov", "h1", Rol.ENFERMERIA, true),
                new Usuario(UUID.randomUUID(), "Beto", "beto@hospital.gov", "h2", Rol.SUPERVISOR, true)
        );
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder);
        List<UsuarioResponseDTO> response = service.listar();

        assertEquals(2, response.size());
        assertEquals("Ana", response.get(0).getNombre());
        assertEquals("Beto", response.get(1).getNombre());
    }

    @Test
    void buscarPorEmail_deberiaRetornarOptionalDeEntidadParaUsoInterno() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        String email = "ana@hospital.gov";
        Usuario usuario = new Usuario(UUID.randomUUID(), "Ana", email, "hash", Rol.ENFERMERIA, true);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        UsuarioService service = new UsuarioService(usuarioRepository, passwordEncoder);
        Optional<Usuario> resultado = service.buscarPorEmail(email);

        assertTrue(resultado.isPresent());
        assertEquals(email, resultado.orElseThrow().getEmail());
        assertEquals("hash", resultado.orElseThrow().getPasswordHash());
    }
}