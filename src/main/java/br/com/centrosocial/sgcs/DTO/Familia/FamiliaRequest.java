package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Familia.AvaliacaoFamilia;
import br.com.centrosocial.sgcs.Models.Familia.TipoResidencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.math.BigDecimal;

public record FamiliaRequest(
        @NotBlank @Size(max = 150) String nome,
        List<@Valid FonteRendaRequest> rendas,
        List<@Valid MembroFamiliaRequest> integrantes,
        String relatos,
        AvaliacaoFamilia avaliacao,
        @NotNull TipoResidencia residencia,
        BigDecimal valorAluguel
) {
    public FamiliaRequest(String nome, List<FonteRendaRequest> rendas,
                          List<MembroFamiliaRequest> integrantes, String relatos,
                          AvaliacaoFamilia avaliacao) {
        this(nome, rendas, integrantes, relatos, avaliacao, TipoResidencia.CEDIDA, null);
    }
}
