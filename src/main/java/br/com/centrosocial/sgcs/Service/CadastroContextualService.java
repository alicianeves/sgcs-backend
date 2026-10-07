package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Atendimento.AtendimentoRequest;
import br.com.centrosocial.sgcs.DTO.Atendimento.AtendimentoResponse;
import br.com.centrosocial.sgcs.DTO.Cadastro.AtendimentoContextualRequest;
import br.com.centrosocial.sgcs.DTO.Cadastro.CadastroContextualRequest;
import br.com.centrosocial.sgcs.DTO.Cadastro.CadastroContextualResponse;
import br.com.centrosocial.sgcs.DTO.Cadastro.ContextoCadastro;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Models.Pessoa.TipoCadastro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroContextualService {
    private final PessoaService pessoaService;
    private final AtendimentoService atendimentoService;

    public CadastroContextualService(PessoaService pessoaService, AtendimentoService atendimentoService) {
        this.pessoaService = pessoaService;
        this.atendimentoService = atendimentoService;
    }

    @Transactional
    public CadastroContextualResponse cadastrar(CadastroContextualRequest request) {
        validarContexto(request);
        PessoaResponse pessoa = pessoaService.cadastrarFisica(request.pessoa());
        AtendimentoResponse atendimento = atendimentoService.cadastrar(toAtendimentoRequest(request, pessoa.id()));
        return resposta(pessoa, atendimento);
    }

    @Transactional
    public CadastroContextualResponse atualizar(Long pessoaId, CadastroContextualRequest request) {
        validarContexto(request);
        PessoaResponse pessoa = pessoaService.atualizarFisica(pessoaId, request.pessoa());
        AtendimentoRequest atendimentoRequest = toAtendimentoRequest(request, pessoa.id());
        AtendimentoResponse atendimento = request.atendimentoId() == null
                ? atendimentoService.cadastrar(atendimentoRequest)
                : atendimentoService.atualizar(request.atendimentoId(), atendimentoRequest);
        return resposta(pessoa, atendimento);
    }

    private void validarContexto(CadastroContextualRequest request) {
        if (request.contexto() != ContextoCadastro.IDOSO)
            throw new BusinessException("O tipo Pessoa deve utilizar o endpoint próprio de Pessoas.");
        if (request.pessoa().tipoCadastro() != TipoCadastro.IDOSO)
            throw new BusinessException("O cadastro contextual de Idoso exige o tipo de cadastro IDOSO.");
        if (request.atendimento() == null)
            throw new BusinessException("Os dados de atendimento são obrigatórios para o cadastro de Idoso.");
    }

    private AtendimentoRequest toAtendimentoRequest(CadastroContextualRequest request, Long pessoaId) {
        AtendimentoContextualRequest atendimento = request.atendimento();
        return new AtendimentoRequest(pessoaId, atendimento.dataAtendimento(), atendimento.bolsaFamilia(),
                atendimento.crasNochete(), atendimento.ubsEsfGuanabara(), atendimento.questionario(),
                atendimento.encaminhamentos(), atendimento.relatos());
    }

    private CadastroContextualResponse resposta(PessoaResponse pessoa, AtendimentoResponse atendimento) {
        return new CadastroContextualResponse(pessoa.id(), pessoa.familiaId(), atendimento.id());
    }
}
