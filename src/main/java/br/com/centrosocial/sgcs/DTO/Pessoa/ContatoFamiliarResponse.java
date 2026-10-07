package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.VinculoFamiliar;

import java.time.LocalDate;

public record ContatoFamiliarResponse(
        Long id,
        String nome,
        LocalDate dataNascimento,
        Integer idade,
        VinculoFamiliar vinculoFamiliar,
        String telefone
) {}
