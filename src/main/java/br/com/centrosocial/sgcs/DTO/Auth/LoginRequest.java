package br.com.centrosocial.sgcs.DTO.Auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String usuario,
        @NotBlank String senha
) {
}
