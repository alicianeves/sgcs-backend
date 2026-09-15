package br.com.centrosocial.sgcs.DTO.Pessoa;

import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaResponse(
        Long id,
        String tipo,
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
        String email,
        String usuario,
        Perfil perfil,
        String razaoSocial,
        String cnpj
) {
}
