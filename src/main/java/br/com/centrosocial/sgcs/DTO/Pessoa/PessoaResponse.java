package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PessoaResponse(
        Long id,
        String tipo,
        TipoCadastro tipoCadastro,
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String estado,
        boolean status,
        LocalDateTime dataCriacao,
        LocalDateTime dataInativacao,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        Integer idade,
        String email,
        String nomeMae,
        Sexo sexo,
        EstadoCivil estadoCivil,
        String rg,
        String nis,
        Escolaridade escolaridade,
        String ocupacao,
        String contato2,
        String usuario,
        Perfil perfil,
        Long familiaId,
        VinculoFamiliar vinculoFamiliar,
        List<ContatoFamiliarResponse> contatosFamiliares,
        String razaoSocial,
        String cnpj
) {}
