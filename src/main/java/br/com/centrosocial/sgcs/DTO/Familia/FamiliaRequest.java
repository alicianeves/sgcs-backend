package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.AvaliacaoFamilia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FamiliaRequest(
        @NotBlank @Size(max = 150) String nome,
        List<@Valid FonteRendaRequest> rendas,
        List<@Valid MembroFamiliaRequest> integrantes,
        String relatos,
        AvaliacaoFamilia avaliacao
) {}
