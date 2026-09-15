package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Pessoa.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    List<Pessoa> findAllByStatusTrue();
}
