package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.AvaliacaoFamilia;

public record FamiliaResumoResponse(Long id, String nome, int quantidadeIntegrantes,
                                    AvaliacaoFamilia avaliacao, boolean status) {}
