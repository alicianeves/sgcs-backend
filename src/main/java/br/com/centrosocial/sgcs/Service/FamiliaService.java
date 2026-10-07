package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Familia.*;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Exception.ResourceNotFoundException;
import br.com.centrosocial.sgcs.Models.Familia.*;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.Pessoa;
import br.com.centrosocial.sgcs.Repository.FamiliaRepository;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;
    private final FisicaRepository fisicaRepository;

    public FamiliaService(FamiliaRepository familiaRepository, FisicaRepository fisicaRepository) {
        this.familiaRepository = familiaRepository;
        this.fisicaRepository = fisicaRepository;
    }

    @Transactional(readOnly = true)
    public List<FamiliaResumoResponse> listar(String busca, Boolean status) {
        String termo = busca == null || busca.isBlank() ? null : busca.trim();
        return familiaRepository.buscar(termo, status).stream().map(this::toResumo).toList();
    }

    @Transactional(readOnly = true)
    public FamiliaResponse buscarPorId(Long id) { return toResponse(buscar(id)); }

    @Transactional
    public FamiliaResponse cadastrar(FamiliaRequest request) {
        Familia familia = new Familia();
        familia.setStatus(true);
        preencher(familia, request);
        familia = familiaRepository.save(familia);
        atualizarComposicao(familia, request.integrantes());
        return toResponse(familia);
    }

    @Transactional
    public FamiliaResponse atualizar(Long id, FamiliaRequest request) {
        Familia familia = buscar(id);
        preencher(familia, request);
        familiaRepository.save(familia);
        atualizarComposicao(familia, request.integrantes());
        return toResponse(familia);
    }

    @Transactional
    public void inativar(Long id) {
        Familia familia = buscar(id);
        if (!familia.isStatus()) return;
        familia.setStatus(false);
        familia.setDataInativacao(LocalDateTime.now());
        familiaRepository.save(familia);
    }

    @Transactional
    public void reativar(Long id) {
        Familia familia = buscar(id);
        familia.setStatus(true);
        familia.setDataInativacao(null);
        familiaRepository.save(familia);
    }

    private Familia buscar(Long id) {
        return familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Família não encontrada."));
    }

    private Fisica buscarFisicaAtiva(Long id) {
        return fisicaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física ativa não encontrada."));
    }

    private void validarDisponibilidade(Fisica pessoa, Long familiaId) {
        if (pessoa.getFamilia() != null && !Objects.equals(pessoa.getFamilia().getId(), familiaId))
            throw new BusinessException("A pessoa física já pertence a outra família.");
    }

    private void preencher(Familia familia, FamiliaRequest r) {
        String nome = normalizar(r.nome());
        if (nome == null) throw new BusinessException("O nome da família é obrigatório.");
        familia.setNome(nome);
        limparRendas(familia);
        Set<TipoRendaBeneficio> tipos = new HashSet<>();
        for (FonteRendaRequest renda : lista(r.rendas())) {
            if (!tipos.add(renda.tipo())) throw new BusinessException("Fonte de renda ou benefício repetido.");
            if (renda.ativa() && renda.valor() == null)
                throw new BusinessException("Informe o valor da fonte de renda ou benefício selecionado.");
            if (renda.valor() != null && renda.valor().signum() < 0) throw new BusinessException("Valores monetários não podem ser negativos.");
            aplicarRenda(familia, renda);
        }
        familia.setRelatos(normalizar(r.relatos()));
        familia.setAvaliacao(r.avaliacao());
        if (r.residencia() == null)
            throw new BusinessException("A residência da família é obrigatória.");
        if (r.valorAluguel() != null && r.valorAluguel().signum() < 0)
            throw new BusinessException("O valor do aluguel não pode ser negativo.");
        if (r.residencia() == TipoResidencia.ALUGADA && r.valorAluguel() == null)
            throw new BusinessException("Informe o valor do aluguel para residência alugada.");
        if (r.residencia() != TipoResidencia.ALUGADA && r.valorAluguel() != null)
            throw new BusinessException("O valor do aluguel somente se aplica à residência alugada.");
        familia.setResidencia(r.residencia());
        familia.setValorAluguel(r.valorAluguel());
    }

    private void atualizarComposicao(Familia familia, List<MembroFamiliaRequest> pedidos) {
        List<Fisica> atuais = fisicaRepository.findAllByFamiliaIdOrderByNome(familia.getId());
        Map<Long, MembroFamiliaRequest> desejados = new LinkedHashMap<>();
        for (MembroFamiliaRequest pedido : lista(pedidos)) {
            if (desejados.putIfAbsent(pedido.pessoaId(), pedido) != null)
                throw new BusinessException("Integrante repetido na composição familiar.");
        }

        for (Fisica atual : atuais) {
            if (!desejados.containsKey(atual.getId())) {
                atual.setFamilia(null);
                atual.setVinculoFamiliar(null);
            }
        }

        for (MembroFamiliaRequest pedido : desejados.values()) {
            Fisica integrante = buscarFisicaAtiva(pedido.pessoaId());
            validarDisponibilidade(integrante, familia.getId());
            integrante.setFamilia(familia);
            integrante.setVinculoFamiliar(pedido.vinculo());
            atuais.add(integrante);
        }
        fisicaRepository.saveAll(atuais.stream().distinct().toList());
    }

    private void limparRendas(Familia f) {
        f.setPossuiRendaTrabalho(false); f.setValorRendaTrabalho(null);
        f.setPossuiAposentadoria(false); f.setValorAposentadoria(null);
        f.setPossuiTransferenciaRenda(false); f.setValorTransferenciaRenda(null);
        f.setPossuiBeneficioMunicipal(false); f.setValorBeneficioMunicipal(null);
        f.setPossuiBpcIdoso(false); f.setValorBpcIdoso(null);
        f.setPossuiBpcDeficiencia(false); f.setValorBpcDeficiencia(null);
        f.setPossuiBolsaFamilia(false); f.setValorBolsaFamilia(null);
    }

    private void aplicarRenda(Familia f, FonteRendaRequest r) {
        BigDecimal valor = r.ativa() ? r.valor() : null;
        switch (r.tipo()) {
            case TRABALHO -> { f.setPossuiRendaTrabalho(r.ativa()); f.setValorRendaTrabalho(valor); }
            case APOSENTADORIA_PENSAO -> { f.setPossuiAposentadoria(r.ativa()); f.setValorAposentadoria(valor); }
            case TRANSFERENCIA_RENDA -> { f.setPossuiTransferenciaRenda(r.ativa()); f.setValorTransferenciaRenda(valor); }
            case BENEFICIO_MUNICIPAL -> { f.setPossuiBeneficioMunicipal(r.ativa()); f.setValorBeneficioMunicipal(valor); }
            case BPC_IDOSO -> { f.setPossuiBpcIdoso(r.ativa()); f.setValorBpcIdoso(valor); }
            case BPC_PESSOA_DEFICIENCIA -> { f.setPossuiBpcDeficiencia(r.ativa()); f.setValorBpcDeficiencia(valor); }
            case BOLSA_FAMILIA -> { f.setPossuiBolsaFamilia(r.ativa()); f.setValorBolsaFamilia(valor); }
        }
    }

    private List<FonteRendaResponse> rendas(Familia f) {
        return List.of(
                new FonteRendaResponse(TipoRendaBeneficio.TRABALHO, f.isPossuiRendaTrabalho(), f.getValorRendaTrabalho()),
                new FonteRendaResponse(TipoRendaBeneficio.APOSENTADORIA_PENSAO, f.isPossuiAposentadoria(), f.getValorAposentadoria()),
                new FonteRendaResponse(TipoRendaBeneficio.TRANSFERENCIA_RENDA, f.isPossuiTransferenciaRenda(), f.getValorTransferenciaRenda()),
                new FonteRendaResponse(TipoRendaBeneficio.BENEFICIO_MUNICIPAL, f.isPossuiBeneficioMunicipal(), f.getValorBeneficioMunicipal()),
                new FonteRendaResponse(TipoRendaBeneficio.BPC_IDOSO, f.isPossuiBpcIdoso(), f.getValorBpcIdoso()),
                new FonteRendaResponse(TipoRendaBeneficio.BPC_PESSOA_DEFICIENCIA, f.isPossuiBpcDeficiencia(), f.getValorBpcDeficiencia()),
                new FonteRendaResponse(TipoRendaBeneficio.BOLSA_FAMILIA, f.isPossuiBolsaFamilia(), f.getValorBolsaFamilia())
        );
    }

    private FamiliaResumoResponse toResumo(Familia f) {
        int quantidade = fisicaRepository.findAllByFamiliaIdOrderByNome(f.getId()).size();
        return new FamiliaResumoResponse(f.getId(), f.getNome(), quantidade, f.getAvaliacao(), f.isStatus());
    }

    private FamiliaResponse toResponse(Familia f) {
        List<Fisica> composicao = fisicaRepository.findAllByFamiliaIdOrderByNome(f.getId());
        List<MembroFamiliaResponse> membros = composicao.stream().map(this::toMembro).toList();
        return new FamiliaResponse(f.getId(), f.getNome(), composicao.size(), rendas(f), membros,
                f.getRelatos(), f.getAvaliacao(),
                f.isStatus(), f.getDataInativacao(), f.getResidencia(), f.getValorAluguel());
    }

    private MembroFamiliaResponse toMembro(Fisica p) {
        return new MembroFamiliaResponse(p.getId(), p.getNome(), p.getCpf(), p.getRg(), p.getNis(),
                p.getDataNascimento(), idade(p), p.getVinculoFamiliar());
    }

    private Integer idade(Fisica p) { return p.getIdadeEfetiva(); }
    private String normalizar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private <T> List<T> lista(List<T> itens) { return itens == null ? List.of() : itens; }
}
