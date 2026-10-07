package br.com.centrosocial.sgcs.DTO.Atendimento;

import br.com.centrosocial.sgcs.Models.Atendimento.RespostaBinaria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RespostaQuestionarioRequest(@Min(1) @Max(12) int numero,
                                          @NotNull RespostaBinaria resposta,
                                          String observacao) {}
