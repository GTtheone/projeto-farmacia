package com.farmacia.repository;

import com.farmacia.model.Movimentacao;
import com.farmacia.model.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByMedicamentoIdOrderByDataMovimentacaoDesc(Long medicamentoId);

    List<Movimentacao> findByTipoOrderByDataMovimentacaoDesc(TipoMovimentacao tipo);

    List<Movimentacao> findByResponsavelContainingIgnoreCaseOrderByDataMovimentacaoDesc(String responsavel);

    @Query("SELECT mv FROM Movimentacao mv WHERE mv.dataMovimentacao BETWEEN :inicio AND :fim ORDER BY mv.dataMovimentacao DESC")
    List<Movimentacao> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<Movimentacao> findByMedicamentoIdAndTipoOrderByDataMovimentacaoDesc(
            Long medicamentoId, TipoMovimentacao tipo
    );

    List<Movimentacao> findAllByOrderByDataMovimentacaoDesc();
}
