package br.com.centrosocial.sgcs.DTO.Atendimento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AtendimentoResponse(Long id, Long fisicaId, String nome, String cpf, Integer idade,
                                  Long familiaId, LocalDate dataAtendimento, boolean bolsaFamilia,
                                  boolean crasNochete, boolean ubsEsfGuanabara,
                                  List<RespostaQuestionarioResponse> questionario,
                                  String encaminhamentos, String relatos, boolean status,
                                  LocalDateTime dataInativacao) {}
