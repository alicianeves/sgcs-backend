package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Atendimento.*;
import br.com.centrosocial.sgcs.DTO.Familia.*;
import br.com.centrosocial.sgcs.DTO.Pessoa.*;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Models.Atendimento.RespostaBinaria;
import br.com.centrosocial.sgcs.Models.Familia.*;
import br.com.centrosocial.sgcs.Models.Pessoa.*;
import br.com.centrosocial.sgcs.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FamiliaAtendimentoIntegrationTests {
    @Autowired PessoaService pessoaService;
    @Autowired FamiliaService familiaService;
    @Autowired AtendimentoService atendimentoService;

    @Test
    void deveExecutarFluxoCompletoDeFamilia() {
        PessoaResponse integranteInicial = criarFisica("Maria", "52998224725");
        PessoaResponse integrante = criarFisica("João", "11144477735");
        FamiliaRequest cadastro = requestFamilia(integranteInicial.id(), integrante.id());
        FamiliaResponse criada = familiaService.cadastrar(cadastro);
        assertEquals(2, criada.quantidadeIntegrantes());
        assertEquals("Família Lima", criada.nome());
        assertEquals(1, familiaService.listar("Lima", true).size());

        FamiliaResponse atualizada = familiaService.atualizar(criada.id(), new FamiliaRequest("Família Souza",
                List.of(new FonteRendaRequest(TipoRendaBeneficio.APOSENTADORIA_PENSAO, true, new BigDecimal("1800.00"))),
                List.of(), "Relato atualizado", AvaliacaoFamilia.REPROVADA));
        assertTrue(atualizada.integrantes().isEmpty());
        assertEquals("Família Souza", atualizada.nome());
        assertEquals(AvaliacaoFamilia.REPROVADA, atualizada.avaliacao());
        familiaService.inativar(criada.id());
        assertFalse(familiaService.buscarPorId(criada.id()).status());
        familiaService.reativar(criada.id());
        assertTrue(familiaService.buscarPorId(criada.id()).status());
    }

    @Test
    void deveRegistrarQuestionarioDaIdosaEmAtendimento() {
        PessoaResponse idosa = criarIdoso("Josefa", "39053344705");
        List<RespostaQuestionarioRequest> respostas = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(i -> new RespostaQuestionarioRequest(i, i % 2 == 0 ? RespostaBinaria.SIM : RespostaBinaria.NAO, "Obs " + i))
                .toList();
        AtendimentoResponse criado = atendimentoService.cadastrar(new AtendimentoRequest(idosa.id(), LocalDate.now(),
                true, true, false, respostas, "Encaminhada ao CRAS", "Acompanhamento iniciado"));
        assertEquals(12, criado.questionario().size());
        assertEquals("Obs 12", criado.questionario().get(11).observacao());
        assertEquals(1, atendimentoService.listar(idosa.id()).size());
        atendimentoService.inativar(criado.id());
        assertFalse(atendimentoService.buscarPorId(criado.id()).status());
        atendimentoService.reativar(criado.id());
        assertTrue(atendimentoService.buscarPorId(criado.id()).status());
    }

    @Test
    void deveCadastrarFamiliaSemIntegrantes() {
        FamiliaResponse criada = familiaService.cadastrar(new FamiliaRequest("Família Oliveira",
                List.of(), null, null, null));

        assertEquals("Família Oliveira", criada.nome());
        assertEquals(0, criada.quantidadeIntegrantes());
        assertTrue(criada.integrantes().isEmpty());
    }

    @Test
    void deveExigirNomeDaFamilia() {
        assertThrows(BusinessException.class, () -> familiaService.cadastrar(
                new FamiliaRequest("  ", List.of(), List.of(), null, null)));
    }

    private FamiliaRequest requestFamilia(Long primeiroIntegrante, Long segundoIntegrante) {
        return new FamiliaRequest(" Família Lima ",
                List.of(new FonteRendaRequest(TipoRendaBeneficio.TRABALHO, true, new BigDecimal("1200.00")),
                        new FonteRendaRequest(TipoRendaBeneficio.BOLSA_FAMILIA, true, new BigDecimal("600.00"))),
                List.of(new MembroFamiliaRequest(primeiroIntegrante, VinculoFamiliar.MAE),
                        new MembroFamiliaRequest(segundoIntegrante, VinculoFamiliar.FILHO_A)),
                "Relato", AvaliacaoFamilia.APROVADA);
    }

    private PessoaResponse criarIdoso(String nome, String cpf) {
        LocalDate nascimentoContato = LocalDate.of(1980, 1, 1);
        ContatoFamiliarRequest contato = new ContatoFamiliarRequest("Contato de " + nome, nascimentoContato,
                Period.between(nascimentoContato, LocalDate.now()).getYears(), VinculoFamiliar.FILHO_A,
                "11988887777");
        return pessoaService.cadastrarFisica(new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro",
                "São Paulo", "SP", nome, cpf, LocalDate.of(1950, 1, 1), null, null, "Mãe", Sexo.FEMININO,
                EstadoCivil.VIUVO_A, "123456", "12345678901", Escolaridade.ENSINO_FUNDAMENTAL, "Aposentada",
                null, TipoCadastro.IDOSO, null, null, List.of(contato), null, null, null));
    }

    private PessoaResponse criarFisica(String nome, String cpf) {
        return pessoaService.cadastrarFisica(new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro",
                "São Paulo", "SP", nome, cpf, LocalDate.of(1950, 1, 1), null, null, "Mãe", Sexo.FEMININO,
                EstadoCivil.VIUVO_A, "123456", "12345678901", Escolaridade.ENSINO_FUNDAMENTAL, "Aposentada",
                null, null, null, null));
    }
}
