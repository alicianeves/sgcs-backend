package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FisicaRepository extends JpaRepository<Fisica, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByUsuario(String usuario);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByUsuarioAndIdNot(String usuario, Long id);
    Optional<Fisica> findByUsuarioAndStatusTrue(String usuario);
}
