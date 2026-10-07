package br.com.centrosocial.sgcs.DTO.Atendimento;

import br.com.centrosocial.sgcs.Models.Atendimento.RespostaBinaria;

public record RespostaQuestionarioResponse(int numero, String pergunta, RespostaBinaria resposta, String observacao) {}
