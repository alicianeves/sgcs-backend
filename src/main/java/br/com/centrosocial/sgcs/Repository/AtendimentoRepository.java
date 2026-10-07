package br.com.centrosocial.sgcs.Repository;

import br.com.centrosocial.sgcs.Models.Atendimento.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    List<Atendimento> findAllByFisicaIdOrderByDataAtendimentoDesc(Long fisicaId);
}
