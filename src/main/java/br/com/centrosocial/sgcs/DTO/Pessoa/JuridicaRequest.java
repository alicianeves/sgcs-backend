package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.TipoCadastro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JuridicaRequest(
        @NotBlank String telefone,
        @NotBlank String cep,
        @NotBlank String logradouro,
        @NotBlank String numero,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank String razaoSocial,
        @NotBlank String cnpj,
        @NotNull TipoCadastro tipoCadastro
) {
    public JuridicaRequest(String telefone, String cep, String logradouro, String numero, String bairro,
                           String cidade, String estado, String razaoSocial, String cnpj) {
        this(telefone, cep, logradouro, numero, bairro, cidade, estado, razaoSocial, cnpj,
                TipoCadastro.PESSOA);
    }
}
