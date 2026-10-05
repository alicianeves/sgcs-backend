package br.com.centrosocial.sgcs.DTO.Familia;

import br.com.centrosocial.sgcs.Models.Pessoa.VinculoFamiliar;
import java.time.LocalDate;

public record MembroFamiliaResponse(Long pessoaId, String nome, String cpf, String rg, String nis,
                                    LocalDate dataNascimento, Integer idade, VinculoFamiliar vinculo) {}
