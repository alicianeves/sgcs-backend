package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Pessoa.Juridica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuridicaRepository extends JpaRepository<Juridica, Long> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
