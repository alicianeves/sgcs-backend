package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Atendimento.RespostaQuestionarioRequest;
import br.com.centrosocial.sgcs.DTO.Cadastro.*;
import br.com.centrosocial.sgcs.DTO.Pessoa.ContatoFamiliarRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Models.Atendimento.RespostaBinaria;
import br.com.centrosocial.sgcs.Models.Pessoa.*;
import br.com.centrosocial.sgcs.Repository.*;
import br.com.centrosocial.sgcs.Service.CadastroContextualService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CadastroContextualIntegrationTests {
    @Autowired CadastroContextualService cadastroContextualService;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired FisicaRepository fisicaRepository;
    @Autowired FamiliaRepository familiaRepository;
    @Autowired AtendimentoRepository atendimentoRepository;

    @Test
    void deveDesfazerPessoaQuandoAtendimentoDoIdosoFalhar() {
        Contagens antes = contagens();
        CadastroContextualRequest request = request("11144477735", atendimento(11));

        assertThrows(BusinessException.class, () -> cadastroContextualService.cadastrar(request));
        assertEquals(antes, contagens());
        assertFalse(fisicaRepository.existsByCpf("11144477735"));
    }

    @Test
    @Transactional
    void devePersistirFichaExistenteDoIdosoComContatoEmUmaUnicaOperacao() {
        CadastroContextualResponse response = cadastroContextualService.cadastrar(
                request("39053344705", atendimento(12)));

        assertNotNull(response.pessoaId());
        assertNull(response.familiaId());
        assertNotNull(response.atendimentoId());
        Fisica idoso = fisicaRepository.findById(response.pessoaId()).orElseThrow();
        assertEquals(TipoCadastro.IDOSO, idoso.getTipoCadastro());
        assertEquals("Maria Familiar", idoso.getContatosFamiliares().get(0).getNome());
        assertEquals("123456", idoso.getRg());
        assertEquals("12345678901", idoso.getNis());
        assertTrue(atendimentoRepository.existsById(response.atendimentoId()));
    }

    @Test
    void deveRejeitarTipoPessoaNoEndpointContextual() {
        FisicaRequest pessoa = pessoaIdosa("52998224725", TipoCadastro.PESSOA, List.of());
        CadastroContextualRequest request = new CadastroContextualRequest(
                ContextoCadastro.PESSOA, pessoa, null, atendimento(12));
        assertThrows(BusinessException.class, () -> cadastroContextualService.cadastrar(request));
    }

    private CadastroContextualRequest request(String cpf, AtendimentoContextualRequest atendimento) {
        return new CadastroContextualRequest(ContextoCadastro.IDOSO,
                pessoaIdosa(cpf, TipoCadastro.IDOSO, List.of(contato())), null, atendimento);
    }

    private FisicaRequest pessoaIdosa(String cpf, TipoCadastro tipo, List<ContatoFamiliarRequest> contatos) {
        return new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro",
                "São Paulo", "SP", "Pessoa Idosa", cpf, LocalDate.of(1950, 1, 1), null,
                null, "Joana", Sexo.FEMININO, EstadoCivil.VIUVO_A, "123456", "12345678901",
                Escolaridade.ENSINO_FUNDAMENTAL, "Aposentada", "11888888888", tipo,
                null, null, contatos, null, null, null);
    }

    private ContatoFamiliarRequest contato() {
        LocalDate nascimento = LocalDate.of(1980, 5, 10);
        return new ContatoFamiliarRequest("Maria Familiar", nascimento,
                Period.between(nascimento, LocalDate.now()).getYears(), VinculoFamiliar.FILHO_A,
                "11977776666");
    }

    private AtendimentoContextualRequest atendimento(int quantidadeRespostas) {
        List<RespostaQuestionarioRequest> respostas = IntStream.rangeClosed(1, quantidadeRespostas)
                .mapToObj(i -> new RespostaQuestionarioRequest(i, RespostaBinaria.SIM, "Observação " + i))
                .toList();
        return new AtendimentoContextualRequest(LocalDate.now(), true, false, true, respostas,
                "Encaminhamento", "Relato da idosa");
    }

    private Contagens contagens() {
        return new Contagens(pessoaRepository.count(), fisicaRepository.count(),
                familiaRepository.count(), atendimentoRepository.count());
    }

    private record Contagens(long pessoas, long fisicas, long familias, long atendimentos) {}
}
