package com.farmacia.service;

import com.farmacia.dto.AlertaDTO;
import com.farmacia.model.Medicamento;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final MedicamentoService medicamentoService;

    @Value("${app.alerta.dias-vencimento:30}")
    private int diasVencimento;


    public List<AlertaDTO> listarTodosAlertas() {
        List<AlertaDTO> alertas = new ArrayList<>();
        alertas.addAll(alertasEstoqueBaixo());
        alertas.addAll(alertasVencimentoProximo());
        alertas.addAll(alertasVencidos());
        return alertas;
    }

    public List<AlertaDTO> alertasEstoqueBaixo() {
        List<Medicamento> medicamentos = medicamentoService.buscarEstoqueAbaixoDoMinimo();

        return medicamentos.stream().map(m -> AlertaDTO.builder()
                .medicamentoId(m.getId())
                .nomeMedicamento(m.getNome())
                .lote(m.getLote())
                .tipoAlerta("ESTOQUE_BAIXO")
                .mensagem(String.format(
                        "Estoque de '%s' (Lote: %s) está abaixo do mínimo! Atual: %d | Mínimo: %d",
                        m.getNome(), m.getLote(), m.getQuantidadeEstoque(), m.getQuantidadeMinima()
                ))
                .quantidadeAtual(m.getQuantidadeEstoque())
                .quantidadeMinima(m.getQuantidadeMinima())
                .build()
        ).toList();
    }


    public List<AlertaDTO> alertasVencimentoProximo() {
        List<Medicamento> medicamentos = medicamentoService.buscarProximosDoVencimento(diasVencimento);

        return medicamentos.stream().map(m -> {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), m.getValidade());
            return AlertaDTO.builder()
                    .medicamentoId(m.getId())
                    .nomeMedicamento(m.getNome())
                    .lote(m.getLote())
                    .tipoAlerta("VENCIMENTO_PROXIMO")
                    .mensagem(String.format(
                            "Medicamento '%s' (Lote: %s) vence em %d dia(s), em %s.",
                            m.getNome(), m.getLote(), dias, m.getValidade()
                    ))
                    .validade(m.getValidade())
                    .diasParaVencer(dias)
                    .build();
        }).toList();
    }

    public List<AlertaDTO> alertasVencidos() {
        List<Medicamento> medicamentos = medicamentoService.buscarVencidos();

        return medicamentos.stream().map(m -> {
            long diasVencido = ChronoUnit.DAYS.between(m.getValidade(), LocalDate.now());
            return AlertaDTO.builder()
                    .medicamentoId(m.getId())
                    .nomeMedicamento(m.getNome())
                    .lote(m.getLote())
                    .tipoAlerta("VENCIDO")
                    .mensagem(String.format(
                            "ATENÇÃO! Medicamento '%s' (Lote: %s) está VENCIDO há %d dia(s). Venceu em: %s.",
                            m.getNome(), m.getLote(), diasVencido, m.getValidade()
                    ))
                    .validade(m.getValidade())
                    .diasParaVencer(-diasVencido)
                    .build();
        }).toList();
    }


    public ResumoAlertas resumoAlertas() {
        return new ResumoAlertas(
                alertasEstoqueBaixo().size(),
                alertasVencimentoProximo().size(),
                alertasVencidos().size()
        );
    }

    public record ResumoAlertas(
            int estoqueBaixo,
            int vencimentoProximo,
            int vencidos
    ) {}
}
