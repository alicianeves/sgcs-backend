package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FisicaRepository extends JpaRepository<Fisica, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByUsuario(String usuario);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByUsuarioAndIdNot(String usuario, Long id);
    Optional<Fisica> findByUsuarioAndStatusTrue(String usuario);
    boolean existsByUsuarioIsNotNull();
    List<Fisica> findAllByFamiliaIdOrderByNome(Long familiaId);
    @Query("""
            select f from Fisica f
            where f.status = :status
              and (:busca is null or lower(f.nome) like lower(concat('%', :busca, '%'))
                   or (:documento is not null and f.cpf like concat('%', :documento, '%')))
            """)
    List<Fisica> buscar(@Param("busca") String busca, @Param("documento") String documento,
                        @Param("status") boolean status);
}
