package com.farmacia.service;

import com.farmacia.dto.MovimentacaoDTO;
import com.farmacia.model.Medicamento;
import com.farmacia.model.Movimentacao;
import com.farmacia.model.enums.TipoMovimentacao;
import com.farmacia.repository.MovimentacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final MedicamentoService medicamentoService;

    @Transactional
    public Movimentacao registrar(MovimentacaoDTO dto) {
        Medicamento medicamento = medicamentoService.buscarPorId(dto.getMedicamentoId());

        boolean isEntrada = dto.getTipo() == TipoMovimentacao.ENTRADA;
        medicamentoService.atualizarEstoque(dto.getMedicamentoId(), dto.getQuantidade(), isEntrada);

        Movimentacao movimentacao = Movimentacao.builder()
                .medicamento(medicamento)
                .tipo(dto.getTipo())
                .quantidade(dto.getQuantidade())
                .responsavel(dto.getResponsavel())
                .observacao(dto.getObservacao())
                .build();

        return movimentacaoRepository.save(movimentacao);
    }


    public List<Movimentacao> listarTodas() {
        return movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc();
    }

    public Movimentacao buscarPorId(Long id) {
        return movimentacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimentação não encontrada com ID: " + id));
    }

    public List<Movimentacao> buscarPorMedicamento(Long medicamentoId) {

        medicamentoService.buscarPorId(medicamentoId);
        return movimentacaoRepository.findByMedicamentoIdOrderByDataMovimentacaoDesc(medicamentoId);
    }

    public List<Movimentacao> buscarPorTipo(TipoMovimentacao tipo) {
        return movimentacaoRepository.findByTipoOrderByDataMovimentacaoDesc(tipo);
    }


    public List<Movimentacao> buscarPorResponsavel(String responsavel) {
        return movimentacaoRepository.findByResponsavelContainingIgnoreCaseOrderByDataMovimentacaoDesc(responsavel);
    }


    public List<Movimentacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim.");
        }
        return movimentacaoRepository.findByPeriodo(inicio, fim);
    }

    public List<Movimentacao> buscarPorMedicamentoETipo(Long medicamentoId, TipoMovimentacao tipo) {
        medicamentoService.buscarPorId(medicamentoId);
        return movimentacaoRepository.findByMedicamentoIdAndTipoOrderByDataMovimentacaoDesc(medicamentoId, tipo);
    }
}
