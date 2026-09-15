package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Auth.LoginRequest;
import br.com.centrosocial.sgcs.DTO.Auth.LoginResponse;
import br.com.centrosocial.sgcs.DTO.Auth.SetupRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.JuridicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import br.com.centrosocial.sgcs.Service.AuthService;
import br.com.centrosocial.sgcs.Service.PessoaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PessoaAuthIntegrationTests {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FisicaRepository fisicaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void deveCadastrarAtualizarInativarEAutenticarPessoaFisica() {
        SetupRequest cadastro = setupRequest("Maria Silva", "maria", "senhaSegura123");

        PessoaResponse criada = authService.configurarAdministradorInicial(cadastro);
        Fisica persistida = fisicaRepository.findById(criada.id()).orElseThrow();

        assertNotEquals(cadastro.senha(), persistida.getSenha());
        assertTrue(passwordEncoder.matches(cadastro.senha(), persistida.getSenha()));
        assertEquals("FISICA", criada.tipo());

        LoginResponse login = authService.login(new LoginRequest("maria", "senhaSegura123"));
        Jwt token = jwtDecoder.decode(login.token());
        List<String> roles = token.getClaimAsStringList("roles");

        assertEquals("maria", token.getSubject());
        assertEquals(criada.id(), token.getClaim("pessoaId"));
        assertEquals(List.of("ROLE_ADMINISTRADOR"), roles);
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));

        String hashAnterior = persistida.getSenha();
        PessoaResponse atualizada = pessoaService.atualizarFisica(
                criada.id(), fisicaRequest("Maria Atualizada", "maria", null)
        );

        assertEquals("Maria Atualizada", atualizada.nome());
        assertEquals(hashAnterior, fisicaRepository.findById(criada.id()).orElseThrow().getSenha());

        pessoaService.inativar(criada.id());

        assertTrue(pessoaService.listar().isEmpty());
        assertFalse(fisicaRepository.findById(criada.id()).orElseThrow().isStatus());
    }

    @Test
    void deveExecutarCrudDePessoaJuridica() {
        JuridicaRequest cadastro = new JuridicaRequest(
                "11999999999", "01001000", "Praça da Sé", "100", "Sé",
                "São Paulo", "SP", "Instituto Social", "12345678000190"
        );

        PessoaResponse criada = pessoaService.cadastrarJuridica(cadastro);
        JuridicaRequest alteracao = new JuridicaRequest(
                "11888888888", "01001000", "Praça da Sé", "101", "Sé",
                "São Paulo", "SP", "Instituto Social Atualizado", "12345678000190"
        );
        PessoaResponse atualizada = pessoaService.atualizarJuridica(criada.id(), alteracao);

        assertEquals("JURIDICA", criada.tipo());
        assertEquals("Instituto Social Atualizado", atualizada.razaoSocial());
        assertEquals(atualizada, pessoaService.buscarPorId(criada.id()));
    }

    private FisicaRequest fisicaRequest(String nome, String usuario, String senha) {
        return new FisicaRequest(
                "11999999999", "01001000", "Praça da Sé", "100", "Sé", "São Paulo", "SP",
                nome, "12345678901", LocalDate.of(1990, 1, 1), "maria@example.com",
                usuario, senha, Perfil.ADMINISTRADOR
        );
    }

    private SetupRequest setupRequest(String nome, String usuario, String senha) {
        return new SetupRequest(
                "11999999999", "01001000", "Praça da Sé", "100", "Sé", "São Paulo", "SP",
                nome, "12345678901", LocalDate.of(1990, 1, 1), "maria@example.com", usuario, senha
        );
    }
}
