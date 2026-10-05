package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.AvaliacaoFamilia;
import java.time.LocalDateTime;
import java.util.List;

public record FamiliaResponse(Long id, String nome, int quantidadeIntegrantes,
                              List<FonteRendaResponse> rendas, List<MembroFamiliaResponse> integrantes,
                              String relatos, AvaliacaoFamilia avaliacao, boolean status,
                              LocalDateTime dataInativacao) {}
