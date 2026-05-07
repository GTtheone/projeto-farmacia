package com.farmacia.service;

import com.farmacia.dto.MedicamentoDTO;
import com.farmacia.model.Medicamento;
import com.farmacia.model.enums.CondicaoArmazenamento;
import com.farmacia.model.enums.FormaAdministracao;
import com.farmacia.repository.MedicamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;


    public List<Medicamento> listarTodos() {
        return medicamentoRepository.findAll();
    }


    public Medicamento buscarPorId(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicamento não encontrado com ID: " + id));
    }


    @Transactional
    public Medicamento cadastrar(MedicamentoDTO dto) {
        Medicamento medicamento = Medicamento.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .lote(dto.getLote())
                .validade(dto.getValidade())
                .dosagem(dto.getDosagem())
                .formaAdministracao(dto.getFormaAdministracao())
                .condicaoArmazenamento(dto.getCondicaoArmazenamento())
                .quantidadeEstoque(dto.getQuantidadeEstoque())
                .quantidadeMinima(dto.getQuantidadeMinima())
                .build();

        return medicamentoRepository.save(medicamento);
    }


    @Transactional
    public Medicamento atualizar(Long id, MedicamentoDTO dto) {
        Medicamento medicamento = buscarPorId(id);

        medicamento.setNome(dto.getNome());
        medicamento.setDescricao(dto.getDescricao());
        medicamento.setLote(dto.getLote());
        medicamento.setValidade(dto.getValidade());
        medicamento.setDosagem(dto.getDosagem());
        medicamento.setFormaAdministracao(dto.getFormaAdministracao());
        medicamento.setCondicaoArmazenamento(dto.getCondicaoArmazenamento());
        medicamento.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        medicamento.setQuantidadeMinima(dto.getQuantidadeMinima());

        return medicamentoRepository.save(medicamento);
    }


    @Transactional
    public void deletar(Long id) {
        Medicamento medicamento = buscarPorId(id);
        medicamentoRepository.delete(medicamento);
    }

  
    public List<Medicamento> buscarPorNome(String nome) {
        return medicamentoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Medicamento> buscarPorLote(String lote) {
        return medicamentoRepository.findByLoteContainingIgnoreCase(lote);
    }

    public List<Medicamento> buscarPorFormaAdministracao(FormaAdministracao forma) {
        return medicamentoRepository.findByFormaAdministracao(forma);
    }

    public List<Medicamento> buscarPorCondicaoArmazenamento(CondicaoArmazenamento condicao) {
        return medicamentoRepository.findByCondicaoArmazenamento(condicao);
    }


    public List<Medicamento> buscarEstoqueAbaixoDoMinimo() {
        return medicamentoRepository.findEstoqueAbaixoDoMinimo();
    }

    public List<Medicamento> buscarProximosDoVencimento(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return medicamentoRepository.findProximosDoVencimento(hoje, limite);
    }

    public List<Medicamento> buscarVencidos() {
        return medicamentoRepository.findVencidos(LocalDate.now());
    }


    @Transactional
    public void atualizarEstoque(Long medicamentoId, int quantidade, boolean entrada) {
        Medicamento medicamento = buscarPorId(medicamentoId);

        if (entrada) {
            medicamento.setQuantidadeEstoque(medicamento.getQuantidadeEstoque() + quantidade);
        } else {
            int novaQuantidade = medicamento.getQuantidadeEstoque() - quantidade;
            if (novaQuantidade < 0) {
                throw new IllegalStateException(
                        "Estoque insuficiente. Quantidade atual: " + medicamento.getQuantidadeEstoque()
                );
            }
            medicamento.setQuantidadeEstoque(novaQuantidade);
        }

        medicamentoRepository.save(medicamento);
    }
}
