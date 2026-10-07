package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

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
        @PastOrPresent LocalDate dataNascimento,
        @PositiveOrZero Integer idadeInformada,
        @Email String email,
        String nomeMae,
        Sexo sexo,
        EstadoCivil estadoCivil,
        String rg,
        String nis,
        Escolaridade escolaridade,
        String ocupacao,
        String contato2,
        @NotNull TipoCadastro tipoCadastro,
        Long familiaId,
        VinculoFamiliar vinculoFamiliar,
        List<@Valid ContatoFamiliarRequest> contatosFamiliares,
        String usuario,
        String senha,
        Perfil perfil
) {
    public FisicaRequest(String telefone, String cep, String logradouro, String numero, String bairro,
                         String cidade, String estado, String nome, String cpf, LocalDate dataNascimento,
                         Integer idadeInformada, String email, String nomeMae, Sexo sexo,
                         EstadoCivil estadoCivil, String rg, String nis, Escolaridade escolaridade,
                         String ocupacao, String contato2, String usuario, String senha, Perfil perfil) {
        this(telefone, cep, logradouro, numero, bairro, cidade, estado, nome, cpf, dataNascimento,
                idadeInformada, email, nomeMae, sexo, estadoCivil, rg, nis, escolaridade, ocupacao,
                contato2, TipoCadastro.PESSOA, null, null, null, usuario, senha, perfil);
    }
}
