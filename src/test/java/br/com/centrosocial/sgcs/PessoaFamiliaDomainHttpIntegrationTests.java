package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Auth.LoginRequest;
import br.com.centrosocial.sgcs.DTO.Auth.SetupRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;
import br.com.centrosocial.sgcs.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PessoaFamiliaDomainHttpIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;

    private String token;

    @BeforeEach
    void autenticarAdministrador() {
        PessoaResponse admin = authService.configurarAdministradorInicial(new SetupRequest(
                "11999999999", "01001000", "Rua Admin", "1", "Centro", "São Paulo", "SP",
                "Administrador", "52998224725", LocalDate.of(1980, 1, 1), null,
                "admin@teste.local", "admin", "senhaAdmin123"));
        token = authService.login(new LoginRequest(admin.usuario(), "senhaAdmin123")).token();
    }

    @Test
    void pfTipoPessoaPodeSerCadastradaSemAcesso() throws Exception {
        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Pessoa sem acesso", "11144477735", "PESSOA", null, List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("FISICA"))
                .andExpect(jsonPath("$.tipoCadastro").value("PESSOA"))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andExpect(jsonPath("$.familiaId").doesNotExist());
    }

    @Test
    void pfTipoPessoaPodeReceberAcesso() throws Exception {
        Map<String, Object> pessoa = fisica("Pessoa com acesso", "11144477735", "PESSOA", null, List.of());
        pessoa.put("usuario", "pessoa.acesso");
        pessoa.put("senha", "senhaSegura123");
        pessoa.put("perfil", Perfil.COLABORADOR);

        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(pessoa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario").value("pessoa.acesso"))
                .andExpect(jsonPath("$.perfil").value("COLABORADOR"));
    }

    @Test
    void idosoExigeContatoEAceitaUmOuVarios() throws Exception {
        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Idoso sem contato", "11144477735", "IDOSO", null, List.of()))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("O cadastro de Idoso deve possuir pelo menos um contato familiar."));

        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Idoso um contato", "39053344705", "IDOSO", null,
                                List.of(contato("Maria", "FILHO_A", "11911112222"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contatosFamiliares.length()").value(1))
                .andExpect(jsonPath("$.contatosFamiliares[0].idade").isNumber())
                .andExpect(jsonPath("$.rg").value("123456"))
                .andExpect(jsonPath("$.nis").value("12345678901"));

        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica("Idoso vários contatos", "93541134780", "IDOSO", null,
                                List.of(contato("João", "FILHO_A", "11922223333"),
                                        contato("Ana", "NETO_A", "11933334444"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contatosFamiliares.length()").value(2));
    }

    @Test
    void idosoNaoPodeReceberAcesso() throws Exception {
        Map<String, Object> idoso = fisica("Idoso inválido", "11144477735", "IDOSO", null,
                List.of(contato("Contato", "FILHO_A", "11911112222")));
        idoso.put("usuario", "idoso.login");
        idoso.put("senha", "senhaSegura123");
        idoso.put("perfil", "COLABORADOR");

        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(idoso)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Pessoa física do tipo Idoso não pode possuir acesso ao sistema."));
    }

    @Test
    void fichaExistenteDoIdosoContinuaFuncionandoComContatos() throws Exception {
        Map<String, Object> pessoa = fisica("Idoso com ficha", "11144477735", "IDOSO", null,
                List.of(contato("Contato da ficha", "FILHO_A", "11911112222")));
        List<Map<String, Object>> questionario = IntStream.rangeClosed(1, 12)
                .mapToObj(numero -> Map.<String, Object>of(
                        "numero", numero, "resposta", numero % 2 == 0 ? "SIM" : "NAO",
                        "observacao", "Observação " + numero))
                .toList();
        Map<String, Object> atendimento = new LinkedHashMap<>();
        atendimento.put("dataAtendimento", LocalDate.now().toString());
        atendimento.put("bolsaFamilia", true);
        atendimento.put("crasNochete", true);
        atendimento.put("ubsEsfGuanabara", false);
        atendimento.put("questionario", questionario);
        atendimento.put("encaminhamentos", "Encaminhamento mantido");
        atendimento.put("relatos", "Relato mantido");

        Map<String, Object> cadastro = new LinkedHashMap<>();
        cadastro.put("contexto", "IDOSO");
        cadastro.put("pessoa", pessoa);
        cadastro.put("atendimento", atendimento);

        String resposta = mockMvc.perform(post("/api/cadastros/contextuais").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(cadastro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaId").isNumber())
                .andExpect(jsonPath("$.familiaId").doesNotExist())
                .andExpect(jsonPath("$.atendimentoId").isNumber())
                .andReturn().getResponse().getContentAsString();
        long atendimentoId = objectMapper.readTree(resposta).get("atendimentoId").asLong();

        mockMvc.perform(get("/api/atendimentos/{id}", atendimentoId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionario.length()").value(12))
                .andExpect(jsonPath("$.encaminhamentos").value("Encaminhamento mantido"))
                .andExpect(jsonPath("$.relatos").value("Relato mantido"));
    }

    @Test
    void pjSomenteAceitaTipoPessoaENuncaDadosDeAcesso() throws Exception {
        mockMvc.perform(post("/api/pessoas/juridicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(juridica("PESSOA"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("JURIDICA"))
                .andExpect(jsonPath("$.tipoCadastro").value("PESSOA"));

        Map<String, Object> idoso = juridica("IDOSO");
        idoso.put("cnpj", "11444777000161");
        mockMvc.perform(post("/api/pessoas/juridicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(idoso)))
                .andExpect(status().isUnprocessableEntity());

        Map<String, Object> comAcesso = juridica("PESSOA");
        comAcesso.put("cnpj", "19131243000197");
        comAcesso.put("usuario", "pj.login");
        comAcesso.put("senha", "senhaSegura123");
        comAcesso.put("perfil", "COLABORADOR");
        mockMvc.perform(post("/api/pessoas/juridicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(comAcesso)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pessoaPodeVincularFamiliaExistenteEOVinculoEhSincronizado() throws Exception {
        long familiaId = cadastrarFamilia("Família Pessoa", "PROPRIA", null, List.of());
        Map<String, Object> pessoa = fisica("Pessoa vinculada", "11144477735", "PESSOA", familiaId, List.of());
        pessoa.put("vinculoFamiliar", "FILHO_A");
        long pessoaId = id(mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(pessoa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.familiaId").value(familiaId))
                .andReturn().getResponse().getContentAsString());

        mockMvc.perform(get("/api/familias/{id}", familiaId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integrantes[0].pessoaId").value(pessoaId))
                .andExpect(jsonPath("$.integrantes[0].vinculo").value("FILHO_A"));
    }

    @Test
    void familiaPodeAdicionarPessoaEOVinculoEhRefletidoNaPessoa() throws Exception {
        long pessoaId = cadastrarPessoa("Pessoa disponível", "11144477735");
        long familiaId = cadastrarFamilia("Família Composta", "ALUGADA", "850.00",
                List.of(membro(pessoaId, "MAE")));

        mockMvc.perform(get("/api/pessoas/{id}", pessoaId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familiaId").value(familiaId))
                .andExpect(jsonPath("$.vinculoFamiliar").value("MAE"));
        mockMvc.perform(get("/api/familias/{id}", familiaId).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residencia").value("ALUGADA"))
                .andExpect(jsonPath("$.valorAluguel").value(850.00));
    }

    @Test
    void camposFamiliaresNaoPertencemMaisAoCadastroDePessoa() throws Exception {
        Map<String, Object> pessoa = fisica("Pessoa inválida", "11144477735", "PESSOA", null, List.of());
        pessoa.put("rendas", List.of());
        pessoa.put("avaliacao", "APROVADA");
        pessoa.put("composicaoFamiliar", List.of());
        pessoa.put("relatos", "Não pertence à Pessoa");
        pessoa.put("residencia", "CEDIDA");

        mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(pessoa)))
                .andExpect(status().isBadRequest());
    }

    private long cadastrarPessoa(String nome, String cpf) throws Exception {
        return id(mockMvc.perform(post("/api/pessoas/fisicas").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(fisica(nome, cpf, "PESSOA", null, List.of()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private long cadastrarFamilia(String nome, String residencia, String valorAluguel,
                                   List<Map<String, Object>> integrantes) throws Exception {
        Map<String, Object> familia = new LinkedHashMap<>();
        familia.put("nome", nome);
        familia.put("rendas", List.of());
        familia.put("integrantes", integrantes);
        familia.put("relatos", "Relato familiar");
        familia.put("avaliacao", "APROVADA");
        familia.put("residencia", residencia);
        familia.put("valorAluguel", valorAluguel);
        return id(mockMvc.perform(post("/api/familias").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(json(familia)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private Map<String, Object> fisica(String nome, String cpf, String tipoCadastro, Long familiaId,
                                       List<Map<String, Object>> contatos) {
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
        dados.put("dataNascimento", "1950-01-01");
        dados.put("nomeMae", "Maria");
        dados.put("sexo", "FEMININO");
        dados.put("estadoCivil", "VIUVO_A");
        dados.put("rg", "123456");
        dados.put("nis", "12345678901");
        dados.put("escolaridade", "ENSINO_FUNDAMENTAL");
        dados.put("ocupacao", "Aposentada");
        dados.put("contato2", "11888888888");
        dados.put("tipoCadastro", tipoCadastro);
        dados.put("familiaId", familiaId);
        dados.put("contatosFamiliares", contatos);
        return dados;
    }

    private Map<String, Object> contato(String nome, String vinculo, String telefone) {
        LocalDate nascimento = LocalDate.of(1980, 5, 10);
        Map<String, Object> contato = new LinkedHashMap<>();
        contato.put("nome", nome);
        contato.put("dataNascimento", nascimento.toString());
        contato.put("idade", Period.between(nascimento, LocalDate.now()).getYears());
        contato.put("vinculoFamiliar", vinculo);
        contato.put("telefone", telefone);
        return contato;
    }

    private Map<String, Object> juridica(String tipoCadastro) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("telefone", "1133334444");
        dados.put("cep", "01001000");
        dados.put("logradouro", "Rua B");
        dados.put("numero", "2");
        dados.put("bairro", "Centro");
        dados.put("cidade", "São Paulo");
        dados.put("estado", "SP");
        dados.put("razaoSocial", "Instituto Social");
        dados.put("cnpj", "11222333000181");
        dados.put("tipoCadastro", tipoCadastro);
        return dados;
    }

    private Map<String, Object> membro(long pessoaId, String vinculo) {
        return Map.of("pessoaId", pessoaId, "vinculo", vinculo);
    }

    private long id(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    private String json(Object value) { return objectMapper.writeValueAsString(value); }
    private String bearer() { return "Bearer " + token; }
}
