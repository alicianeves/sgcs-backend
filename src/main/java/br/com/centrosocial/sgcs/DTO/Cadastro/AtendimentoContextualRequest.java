package br.com.centrosocial.sgcs.DTO.Cadastro;

import br.com.centrosocial.sgcs.DTO.Atendimento.RespostaQuestionarioRequest;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public record AtendimentoContextualRequest(
        LocalDate dataAtendimento,
        boolean bolsaFamilia,
        boolean crasNochete,
        boolean ubsEsfGuanabara,
        List<@Valid RespostaQuestionarioRequest> questionario,
        String encaminhamentos,
        String relatos
) {}
