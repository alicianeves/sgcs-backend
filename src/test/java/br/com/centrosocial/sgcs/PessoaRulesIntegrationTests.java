package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Pessoa.*;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Service.PessoaService;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PessoaRulesIntegrationTests {
    @Autowired PessoaService pessoaService;
    @Autowired FisicaRepository fisicaRepository;

    @Test
    void deveUsarIdadeInformadaSomenteQuandoNascimentoForDesconhecido() {
        FisicaRequest semNascimento = fisicaComIdade("Pessoa sem nascimento", "52998224725", null, 74);
        PessoaResponse criada = pessoaService.cadastrarFisica(semNascimento);
        assertNull(criada.dataNascimento());
        assertEquals(74, criada.idade());
        assertEquals(74, fisicaRepository.findById(criada.id()).orElseThrow().getIdadeInformada());

        FisicaRequest comNascimento = fisicaComIdade("Pessoa com nascimento", "52998224725",
                LocalDate.now().minusYears(50), 99);
        PessoaResponse atualizada = pessoaService.atualizarFisica(criada.id(), comNascimento);
        assertEquals(50, atualizada.idade());
        assertNull(fisicaRepository.findById(criada.id()).orElseThrow().getIdadeInformada());

        assertThrows(BusinessException.class, () -> pessoaService.cadastrarFisica(
                fisicaComIdade("Sem idade", "11144477735", null, null)));
    }

    @Test
    void deveManterDocumentosImutaveis() {
        PessoaResponse fisica = pessoaService.cadastrarFisica(fisica("Maria Souza", "52998224725"));
        FisicaRequest cpfAlterado = fisica("Maria Souza", "11144477735");
        assertThrows(BusinessException.class, () -> pessoaService.atualizarFisica(fisica.id(), cpfAlterado));

        JuridicaRequest juridica = juridica("Instituto Social", "11222333000181");
        PessoaResponse pj = pessoaService.cadastrarJuridica(juridica);
        assertThrows(BusinessException.class, () -> pessoaService.atualizarJuridica(
                pj.id(), juridica("Instituto Social", "11444777000161")));
    }

    @Test
    void deveListarBuscarConsultarInativosEReativarAmbosOsTipos() {
        PessoaResponse fisica = pessoaService.cadastrarFisica(fisica("Maria da Silva", "52998224725"));
        PessoaResponse juridica = pessoaService.cadastrarJuridica(juridica("Instituto Esperança", "11222333000181"));

        assertEquals(fisica.id(), pessoaService.listar("Maria da", true).get(0).id());
        assertEquals(fisica.id(), pessoaService.listar("982247", true).get(0).id());
        assertEquals(juridica.id(), pessoaService.listar("Esperança", true).get(0).id());
        assertEquals(juridica.id(), pessoaService.listar("223330001", true).get(0).id());

        pessoaService.inativar(fisica.id());
        pessoaService.inativar(juridica.id());
        assertTrue(pessoaService.listar(null, true).isEmpty());
        assertEquals(2, pessoaService.listar(null, false).size());
        assertFalse(pessoaService.buscarPorId(fisica.id()).status());
        assertFalse(pessoaService.buscarPorId(juridica.id()).status());

        pessoaService.reativar(fisica.id());
        pessoaService.reativar(juridica.id());
        assertEquals(2, pessoaService.listar(null, true).size());
        assertTrue(pessoaService.buscarPorId(fisica.id()).status());
        assertTrue(pessoaService.buscarPorId(juridica.id()).status());
    }

    private FisicaRequest fisica(String nome, String cpf) {
        return fisicaComIdade(nome, cpf, LocalDate.of(1980, 1, 1), null);
    }

    private FisicaRequest fisicaComIdade(String nome, String cpf, LocalDate nascimento, Integer idadeInformada) {
        return new FisicaRequest("11999999999", "01001000", "Rua A", "1", "Centro", "São Paulo", "SP",
                nome, cpf, nascimento, idadeInformada, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    private JuridicaRequest juridica(String nome, String cnpj) {
        return new JuridicaRequest("11999999999", "01001000", "Rua B", "2", "Centro", "São Paulo", "SP",
                nome, cnpj);
    }
}
