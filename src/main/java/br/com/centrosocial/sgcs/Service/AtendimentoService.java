package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Atendimento.*;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Exception.ResourceNotFoundException;
import br.com.centrosocial.sgcs.Models.Atendimento.Atendimento;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.Pessoa;
import br.com.centrosocial.sgcs.Models.Pessoa.TipoCadastro;
import br.com.centrosocial.sgcs.Repository.AtendimentoRepository;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AtendimentoService {
    private static final List<String> PERGUNTAS = List.of(
            "Você se sente satisfeita com a sua vida?", "Sente-se frequentemente aborrecida?",
            "Tem pensamentos negativos?", "Você tem liberdade de tomar suas próprias decisões?",
            "Sente-se feliz na maior parte do tempo?", "Você abandonou muitas coisas que fazia ou gostaria de fazer?",
            "Sente vontade de chorar com frequência?", "A sua memória tem funcionado bem?",
            "Sente-se angustiada sem causa específica?", "Tem dormido bem?",
            "Faz uso de alguma medicação?", "Tem alguma doença?"
    );
    private final AtendimentoRepository atendimentoRepository;
    private final FisicaRepository fisicaRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository, FisicaRepository fisicaRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.fisicaRepository = fisicaRepository;
    }

    @Transactional(readOnly = true)
    public List<AtendimentoResponse> listar(Long fisicaId) {
        List<Atendimento> registros = fisicaId == null ? atendimentoRepository.findAll()
                : atendimentoRepository.findAllByFisicaIdOrderByDataAtendimentoDesc(fisicaId);
        return registros.stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public AtendimentoResponse buscarPorId(Long id) { return toResponse(buscar(id)); }

    @Transactional
    public AtendimentoResponse cadastrar(AtendimentoRequest request) {
        Atendimento atendimento = new Atendimento();
        atendimento.setFisica(buscarFisicaAtiva(request.fisicaId()));
        atendimento.setDataAtendimento(request.dataAtendimento() == null ? LocalDate.now() : request.dataAtendimento());
        atendimento.setStatus(true);
        preencher(atendimento, request);
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public AtendimentoResponse atualizar(Long id, AtendimentoRequest request) {
        Atendimento atendimento = buscar(id);
        atendimento.setFisica(buscarFisicaAtiva(request.fisicaId()));
        if (request.dataAtendimento() != null) atendimento.setDataAtendimento(request.dataAtendimento());
        preencher(atendimento, request);
        return toResponse(atendimentoRepository.save(atendimento));
    }

    @Transactional
    public void inativar(Long id) { Atendimento a = buscar(id); a.setStatus(false); a.setDataInativacao(LocalDateTime.now()); }
    @Transactional
    public void reativar(Long id) { Atendimento a = buscar(id); a.setStatus(true); a.setDataInativacao(null); }

    private Atendimento buscar(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atendimento não encontrado."));
    }
    private Fisica buscarFisicaAtiva(Long id) {
        Fisica fisica = fisicaRepository.findById(id).filter(Pessoa::isStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física ativa não encontrada."));
        if (fisica.getTipoCadastro() != TipoCadastro.IDOSO)
            throw new BusinessException("Atendimentos da ficha de Idoso exigem uma pessoa do tipo Idoso.");
        return fisica;
    }
    private void preencher(Atendimento a, AtendimentoRequest r) {
        a.setBolsaFamilia(r.bolsaFamilia());
        a.setCrasNochete(r.crasNochete());
        a.setUbsEsfGuanabara(r.ubsEsfGuanabara());
        List<RespostaQuestionarioRequest> respostas = r.questionario() == null ? List.of() : r.questionario();
        if (respostas.size() != 12) throw new BusinessException("O questionário deve conter as 12 respostas.");
        Set<Integer> numeros = new HashSet<>();
        for (RespostaQuestionarioRequest resposta : respostas) {
            if (!numeros.add(resposta.numero())) throw new BusinessException("Pergunta repetida no questionário.");
            a.setResposta(resposta.numero(), resposta.resposta());
            a.setObservacao(resposta.numero(), normalizar(resposta.observacao()));
        }
        a.setEncaminhamentos(normalizar(r.encaminhamentos()));
        a.setRelatos(normalizar(r.relatos()));
    }
    private AtendimentoResponse toResponse(Atendimento a) {
        Fisica f = a.getFisica();
        List<RespostaQuestionarioResponse> respostas = new ArrayList<>();
        for (int i = 1; i <= 12; i++) respostas.add(new RespostaQuestionarioResponse(i, PERGUNTAS.get(i - 1), a.getResposta(i), a.getObservacao(i)));
        return new AtendimentoResponse(a.getId(), f.getId(), f.getNome(), f.getCpf(), f.getIdadeEfetiva(),
                f.getFamilia() == null ? null : f.getFamilia().getId(), a.getDataAtendimento(), a.isBolsaFamilia(),
                a.isCrasNochete(), a.isUbsEsfGuanabara(), respostas, a.getEncaminhamentos(), a.getRelatos(),
                a.isStatus(), a.getDataInativacao());
    }
    private String normalizar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
