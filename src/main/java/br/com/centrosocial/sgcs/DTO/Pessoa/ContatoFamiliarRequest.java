package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.VinculoFamiliar;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ContatoFamiliarRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull @PastOrPresent LocalDate dataNascimento,
        @NotNull @PositiveOrZero Integer idade,
        @NotNull VinculoFamiliar vinculoFamiliar,
        @NotBlank @Size(max = 20) String telefone
) {}
