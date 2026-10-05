package br.com.centrosocial.sgcs;

import br.com.centrosocial.sgcs.DTO.Familia.FamiliaRequest;
import br.com.centrosocial.sgcs.DTO.Familia.FamiliaResponse;
import br.com.centrosocial.sgcs.DTO.Familia.MembroFamiliaRequest;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.VinculoFamiliar;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import br.com.centrosocial.sgcs.Service.FamiliaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FamiliaSimplificadaIntegrationTests {
    @Autowired FamiliaService familiaService;
    @Autowired FisicaRepository fisicaRepository;

    @Test
    void deveCadastrarEListarFamiliaSemIntegrantes() {
        FamiliaResponse familia = familiaService.cadastrar(
                new FamiliaRequest(" Família Oliveira ", List.of(), null, "Relato", null));

        assertEquals("Família Oliveira", familia.nome());
        assertEquals(0, familia.quantidadeIntegrantes());
        assertTrue(familia.integrantes().isEmpty());
        assertEquals("Família Oliveira", familiaService.listar("Oliveira", true).get(0).nome());
    }

    @Test
    void deveVincularPessoaJaCadastradaAComposicao() {
        Fisica pessoa = new Fisica();
        pessoa.setNome("Maria Souza");
        pessoa.setCpf("52998224725");
        pessoa.setStatus(true);
        pessoa = fisicaRepository.save(pessoa);

        FamiliaResponse familia = familiaService.cadastrar(new FamiliaRequest("Família Souza", List.of(),
                List.of(new MembroFamiliaRequest(pessoa.getId(), VinculoFamiliar.MAE)), null, null));

        assertEquals(1, familia.quantidadeIntegrantes());
        assertEquals(pessoa.getId(), familia.integrantes().get(0).pessoaId());
        assertEquals(familia.id(), fisicaRepository.findById(pessoa.getId()).orElseThrow().getFamilia().getId());
    }

    @Test
    void deveExigirNomeDaFamilia() {
        assertThrows(BusinessException.class, () -> familiaService.cadastrar(
                new FamiliaRequest(" ", List.of(), List.of(), null, null)));
    }
}
