package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Auth.*;
import br.com.centrosocial.sgcs.DTO.Pessoa.*;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Exception.ConflictException;
import br.com.centrosocial.sgcs.Models.Pessoa.*;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import br.com.centrosocial.sgcs.Service.AuthService;
import br.com.centrosocial.sgcs.Service.PessoaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PessoaAuthIntegrationTests {
    @Autowired PessoaService pessoaService;
    @Autowired AuthService authService;
    @Autowired FisicaRepository fisicaRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;

    @Test
    void deveCadastrarPessoaComAcessoEAutenticar() {
        SetupRequest cadastro = new SetupRequest("11999999999", "01001000", "Praça da Sé", "100", "Sé",
                "São Paulo", "SP", "Maria Silva", "52998224725", LocalDate.of(1990, 1, 1), null,
                "maria@example.com", "maria", "senhaSegura123");
        PessoaResponse criada = authService.configurarAdministradorInicial(cadastro);
        Fisica persistida = fisicaRepository.findById(criada.id()).orElseThrow();
        assertTrue(passwordEncoder.matches(cadastro.senha(), persistida.getSenha()));
        LoginResponse login = authService.login(new LoginRequest("maria", "senhaSegura123"));
        Jwt token = jwtDecoder.decode(login.token());
        assertEquals("maria", token.getSubject());
        assertEquals(List.of("ROLE_ADMINISTRADOR"), token.getClaimAsStringList("roles"));
    }

    @Test
    void deveCadastrarPessoaSemAcessoComDadosComplementares() {
        PessoaResponse criada = pessoaService.cadastrarFisica(fisica("Ana", "11144477735", null, null, null));
        Fisica fisica = fisicaRepository.findById(criada.id()).orElseThrow();
        assertNull(fisica.getUsuario());
        assertEquals("Joana", criada.nomeMae());
        assertEquals(Sexo.FEMININO, criada.sexo());
        assertEquals(42, criada.idade());
    }

    @Test
    void deveValidarDuplicidadeEDataFutura() {
        pessoaService.cadastrarFisica(fisica("Ana", "11144477735", null, null, null));
        assertThrows(ConflictException.class, () -> pessoaService.cadastrarFisica(fisica("Outra", "11144477735", null, null, null)));
        FisicaRequest futura = new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro", "São Paulo", "SP",
                "Futura", "39053344705", LocalDate.now().plusDays(1), null, null, null, null, null, null, null, null, null,
                null, null, null, null);
        assertThrows(BusinessException.class, () -> pessoaService.cadastrarFisica(futura));
    }

    @Test
    void deveExecutarCrudJuridica() {
        JuridicaRequest cadastro = new JuridicaRequest("11999999999", "01001000", "Praça", "100", "Sé",
                "São Paulo", "SP", "Instituto Social", "11222333000181");
        PessoaResponse criada = pessoaService.cadastrarJuridica(cadastro);
        assertEquals("JURIDICA", criada.tipo());
        PessoaResponse atualizada = pessoaService.atualizarJuridica(criada.id(), new JuridicaRequest("11888888888",
                "01001000", "Praça", "101", "Sé", "São Paulo", "SP", "Instituto Atualizado", "11222333000181"));
        assertEquals("Instituto Atualizado", atualizada.razaoSocial());
        pessoaService.inativar(criada.id());
        assertFalse(pessoaService.listar().contains(criada));
    }

    private FisicaRequest fisica(String nome, String cpf, String usuario, String senha, Perfil perfil) {
        return new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro", "São Paulo", "SP",
                nome, cpf, LocalDate.of(1984, 1, 1), null, "ana@example.com", "Joana", Sexo.FEMININO,
                EstadoCivil.SOLTEIRO_A, "123456", "12345678901", Escolaridade.ENSINO_MEDIO, "Artesã",
                "11888888888", usuario, senha, perfil);
    }
}
