package br.com.centrosocial.sgcs.DTO.Pessoa;

import jakarta.validation.constraints.NotBlank;

public record JuridicaRequest(
        @NotBlank String telefone,
        @NotBlank String cep,
        @NotBlank String logradouro,
        @NotBlank String numero,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank String razaoSocial,
        @NotBlank String cnpj
) {
}
