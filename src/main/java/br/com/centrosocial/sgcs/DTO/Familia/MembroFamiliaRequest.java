package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Pessoa.VinculoFamiliar;
import jakarta.validation.constraints.NotNull;

public record MembroFamiliaRequest(@NotNull Long pessoaId, @NotNull VinculoFamiliar vinculo) {}
