package co.edu.uceva.inventariosibe;

import co.edu.uceva.inventariosibe.usuario.Rol;
import co.edu.uceva.inventariosibe.usuario.Usuario;
import co.edu.uceva.inventariosibe.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioSibeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenSupervisor;

    @BeforeEach
    void limpiarYCrearSupervisor() throws Exception {
        usuarioRepository.deleteAll();

        Usuario supervisor = new Usuario(
                null,
                "Supervisor Test",
                "supervisor@hospital.gov",
                passwordEncoder.encode("supervisorPassword123"),
                Rol.SUPERVISOR,
                true
        );
        usuarioRepository.save(supervisor);

        String loginBody = """
                {"email":"supervisor@hospital.gov","password":"supervisorPassword123"}
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        int tokenIndex = response.indexOf("\"token\":\"") + 9;
        int tokenEnd = response.indexOf("\"", tokenIndex);
        tokenSupervisor = response.substring(tokenIndex, tokenEnd);
    }

    @Test
    void login_retornaJwtToken_valido() throws Exception {
        assertThat(tokenSupervisor).isNotBlank();
        assertThat(tokenSupervisor.split("\\.")).hasSize(3);
    }

    @Test
    void crearUsuario_endToEnd_hasheaBcryptYNoExponePasswordHash() throws Exception {
        String body = """
            {"nombre":"Ana","email":"ana@hospital.gov",
             "password":"plainPassword123","rol":"ENFERMERIA","activo":true}
            """;

        String response = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenSupervisor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("passwordHash");
        assertThat(response).doesNotContain("plainPassword123");
        assertThat(response).contains("ana@hospital.gov");
    }

    @Test
    void crearUsuario_sinToken_retorna403_o_401() throws Exception {
        String body = """
            {"nombre":"Ana","email":"ana@hospital.gov",
             "password":"plainPassword123","rol":"ENFERMERIA","activo":true}
            """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void noSuchElementException_retorna404ConMensajeYTimestamp() throws Exception {
        UUID inexistente = UUID.randomUUID();
        mockMvc.perform(get("/api/usuarios/" + inexistente)
                        .header("Authorization", "Bearer " + tokenSupervisor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}