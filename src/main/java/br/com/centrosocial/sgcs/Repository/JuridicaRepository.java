package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Pessoa.Juridica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JuridicaRepository extends JpaRepository<Juridica, Long> {
    boolean existsByCnpj(String cnpj);
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
    @Query("""
            select j from Juridica j
            where j.status = :status
              and (:busca is null or lower(j.razaoSocial) like lower(concat('%', :busca, '%'))
                   or (:documento is not null and j.cnpj like concat('%', :documento, '%')))
            """)
    List<Juridica> buscar(@Param("busca") String busca, @Param("documento") String documento,
                          @Param("status") boolean status);
}
