package com.farmacia.repository;

import com.farmacia.model.Medicamento;
import com.farmacia.model.enums.CondicaoArmazenamento;
import com.farmacia.model.enums.FormaAdministracao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByNomeContainingIgnoreCase(String nome);

    List<Medicamento> findByLoteContainingIgnoreCase(String lote);

    List<Medicamento> findByFormaAdministracao(FormaAdministracao formaAdministracao);

    List<Medicamento> findByCondicaoArmazenamento(CondicaoArmazenamento condicaoArmazenamento);

    @Query("SELECT m FROM Medicamento m WHERE m.quantidadeEstoque < m.quantidadeMinima")
    List<Medicamento> findEstoqueAbaixoDoMinimo();

    @Query("SELECT m FROM Medicamento m WHERE m.validade BETWEEN :hoje AND :limite")
    List<Medicamento> findProximosDoVencimento(
            @Param("hoje") LocalDate hoje,
            @Param("limite") LocalDate limite
    );

    @Query("SELECT m FROM Medicamento m WHERE m.validade < :hoje")
    List<Medicamento> findVencidos(@Param("hoje") LocalDate hoje);
}
