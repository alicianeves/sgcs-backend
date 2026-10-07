package br.com.centrosocial.sgcs.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SetupRequest(
        @NotBlank String telefone,
        @NotBlank String cep,
        @NotBlank String logradouro,
        @NotBlank String numero,
        @NotBlank String bairro,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank String nome,
        @NotBlank String cpf,
        @PastOrPresent LocalDate dataNascimento,
        @PositiveOrZero Integer idadeInformada,
        @Email @NotBlank String email,
        @NotBlank String usuario,
        @NotBlank @Size(min = 8, message = "deve ter no mínimo 8 caracteres") String senha
) {
}
