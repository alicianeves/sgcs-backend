package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FisicaRequest(
        @NotBlank String telefone,
        @NotBlank String cep,
        @NotBlank String logradouro,
        @NotBlank String numero,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank String nome,
        @NotBlank String cpf,
        @NotNull LocalDate dataNascimento,
        @Email @NotBlank String email,
        @NotBlank String usuario,
        @Size(min = 8, message = "deve ter no mínimo 8 caracteres") String senha,
        @NotNull Perfil perfil
) {
}
