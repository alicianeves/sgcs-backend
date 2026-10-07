package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Auth.*;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;
import br.com.centrosocial.sgcs.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PessoaAuthorizationHttpIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;

    private PessoaResponse admin;
    private String adminToken;

    @BeforeEach
    void criarAdministrador() {
        admin = authService.configurarAdministradorInicial(new SetupRequest(
                "11999999999", "01001000", "Rua Admin", "1", "Centro", "São Paulo", "SP",
                "Administrador", "52998224725", LocalDate.of(1980, 1, 1), null,
                "admin@sgcs.test", "admin", "senhaAdmin123"));
        adminToken = authService.login(new LoginRequest("admin", "senhaAdmin123")).token();
    }

    @Test
    void administradorPodeConcederEAlterarAcessoDeOutraPessoa() throws Exception {
        mockMvc.perform(post("/api/cadastros/contextuais")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        long id = cadastrarSemAcesso("Pessoa Alvo", "11144477735");
        mockMvc.perform(put("/api/pessoas/fisicas/{id}", id)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Pessoa Alvo", "11144477735", "novo.usuario",
                                "senhaNova123", Perfil.COLABORADOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("novo.usuario"))
                .andExpect(jsonPath("$.perfil").value("COLABORADOR"));
        mockMvc.perform(delete("/api/pessoas/fisicas/{id}/acesso", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pessoas/{id}", id).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.perfil").doesNotExist());
    }

    @Test
    void cadastroDeFamiliaUsaFormularioProprioESemPessoaObrigatoria() throws Exception {
        Map<String, Object> familia = new LinkedHashMap<>();
        familia.put("nome", "Família Oliveira");
        familia.put("rendas", List.of());
        familia.put("relatos", "Cadastro independente de Pessoa");
        familia.put("residencia", "CEDIDA");

        mockMvc.perform(post("/api/familias")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(familia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Família Oliveira"))
                .andExpect(jsonPath("$.quantidadeIntegrantes").value(0))
                .andExpect(jsonPath("$.integrantes").isEmpty());

        mockMvc.perform(get("/api/familias?busca=Oliveira")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Família Oliveira"));

        familia.put("nome", " ");
        mockMvc.perform(post("/api/familias")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(familia)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atendimentoNaoPodeConcederRemoverOuAlterarPerfilMasPodeEditarDadosPessoais() throws Exception {
        long gestorId = cadastrarComAcesso("Gestora", "11144477735", "gestora",
                "senhaGestora123", Perfil.ATENDIMENTO_GESTAO);
        String gestorToken = authService.login(new LoginRequest("gestora", "senhaGestora123")).token();
        long semAcesso = cadastrarSemAcesso("Sem Acesso", "39053344705");
        long comAcesso = cadastrarComAcesso("Colaborador", "93541134780", "colaborador",
                "senhaColab123", Perfil.COLABORADOR);

        mockMvc.perform(put("/api/pessoas/fisicas/{id}", semAcesso)
                        .header("Authorization", bearer(gestorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Sem Acesso", "39053344705", "concedido",
                                "senhaNova123", Perfil.COLABORADOR))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/pessoas/fisicas/{id}/acesso", comAcesso)
                        .header("Authorization", bearer(gestorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/pessoas/fisicas/{id}", comAcesso)
                        .header("Authorization", bearer(gestorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Colaborador", "93541134780", "colaborador",
                                null, Perfil.ADMINISTRADOR))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/pessoas/fisicas/{id}", gestorId)
                        .header("Authorization", bearer(gestorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Gestora Atualizada", "11144477735", null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Gestora Atualizada"));

        mockMvc.perform(post("/api/cadastros/contextuais")
                        .header("Authorization", bearer(gestorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void familiaNaoEhMaisTipoDeCadastroEProfessorPermaneceBloqueado() throws Exception {
        cadastrarComAcesso("Colaborador", "11144477735", "colaborador",
                "senhaColab123", Perfil.COLABORADOR);
        cadastrarComAcesso("Professor", "39053344705", "professor",
                "senhaProf123", Perfil.PROFESSOR_INSTRUTOR);
        String colaboradorToken = authService.login(new LoginRequest("colaborador", "senhaColab123")).token();
        String professorToken = authService.login(new LoginRequest("professor", "senhaProf123")).token();
        long alvo = cadastrarSemAcesso("Alvo", "93541134780");

        mockMvc.perform(put("/api/pessoas/fisicas/{id}", alvo)
                        .header("Authorization", bearer(colaboradorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Alvo", "93541134780", "alvo.login",
                                "senhaAlvo123", Perfil.COLABORADOR))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/pessoas").header("Authorization", bearer(professorToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/familias").header("Authorization", bearer(professorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/cadastros/contextuais")
                        .header("Authorization", bearer(colaboradorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(cadastroContextual("Responsavel", "12345678909", null))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/pessoas/fisicas")
                        .header("Authorization", bearer(colaboradorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Responsavel", "12345678909", null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCadastro").value("PESSOA"));

        mockMvc.perform(get("/api/atendimentos").header("Authorization", bearer(colaboradorToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/cadastros/contextuais")
                        .header("Authorization", bearer(professorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoAlteraProprioPerfilNemInativaASiMesmo() throws Exception {
        mockMvc.perform(put("/api/pessoas/fisicas/{id}", admin.id())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Administrador", "52998224725", "admin",
                                null, Perfil.COLABORADOR))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(delete("/api/pessoas/{id}", admin.id())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void tokenDePessoaInativadaPerdeValidadeImediatamente() throws Exception {
        long id = cadastrarComAcesso("Gestora", "11144477735", "gestora",
                "senhaGestora123", Perfil.ATENDIMENTO_GESTAO);
        String token = authService.login(new LoginRequest("gestora", "senhaGestora123")).token();
        mockMvc.perform(delete("/api/pessoas/{id}", id).header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pessoas").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    private long cadastrarSemAcesso(String nome, String cpf) throws Exception {
        return cadastrar(nome, cpf, null, null, null);
    }

    private long cadastrarComAcesso(String nome, String cpf, String usuario,
                                    String senha, Perfil perfil) throws Exception {
        return cadastrar(nome, cpf, usuario, senha, perfil);
    }

    private long cadastrar(String nome, String cpf, String usuario, String senha, Perfil perfil) throws Exception {
        String response = mockMvc.perform(post("/api/pessoas/fisicas")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica(nome, cpf, usuario, senha, perfil))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Map<String, Object> fisica(String nome, String cpf, String usuario, String senha, Perfil perfil) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("telefone", "11999999999");
        dados.put("cep", "01001000");
        dados.put("logradouro", "Rua A");
        dados.put("numero", "1");
        dados.put("bairro", "Centro");
        dados.put("cidade", "São Paulo");
        dados.put("estado", "SP");
        dados.put("nome", nome);
        dados.put("cpf", cpf);
        dados.put("dataNascimento", "1980-01-01");
        dados.put("tipoCadastro", "PESSOA");
        dados.put("usuario", usuario);
        dados.put("senha", senha);
        dados.put("perfil", perfil);
        return dados;
    }

    private Map<String, Object> cadastroContextual(String nome, String cpf, Long familiaId) {
        Map<String, Object> familia = new LinkedHashMap<>();
        familia.put("nome", "Família Contextual");
        familia.put("rendas", List.of());
        familia.put("integrantes", List.of());

        Map<String, Object> cadastro = new LinkedHashMap<>();
        cadastro.put("contexto", "FAMILIA");
        cadastro.put("pessoa", fisica(nome, cpf, null, null, null));
        cadastro.put("familiaId", familiaId);
        cadastro.put("familia", familia);
        return cadastro;
    }

    private String json(Object value) { return objectMapper.writeValueAsString(value); }
    private String bearer(String token) { return "Bearer " + token; }
}
