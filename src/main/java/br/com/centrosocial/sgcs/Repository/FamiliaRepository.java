package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Familia.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FamiliaRepository extends JpaRepository<Familia, Long> {
    @Query("""
            select f from Familia f
            where (:status is null or f.status = :status)
              and (:busca is null or lower(f.nome) like lower(concat('%', :busca, '%')))
            order by f.nome, f.id
            """)
    List<Familia> buscar(@Param("busca") String busca, @Param("status") Boolean status);
}
