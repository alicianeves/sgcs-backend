package br.com.centrosocial.sgcs.DTO.Cadastro;

import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CadastroContextualRequest(
        @NotNull ContextoCadastro contexto,
        @NotNull @Valid FisicaRequest pessoa,
        Long atendimentoId,
        @Valid AtendimentoContextualRequest atendimento
) {}
