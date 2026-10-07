package br.com.centrosocial.sgcs.DTO.Atendimento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AtendimentoRequest(@NotNull Long fisicaId, LocalDate dataAtendimento,
                                 boolean bolsaFamilia, boolean crasNochete, boolean ubsEsfGuanabara,
                                 List<@Valid RespostaQuestionarioRequest> questionario,
                                 String encaminhamentos, String relatos) {}
