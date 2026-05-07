package com.farmacia.model;

import com.farmacia.model.enums.TipoMovimentacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Medicamento")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @NotNull(message = "Tipo de movimentação")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @NotNull(message = "Quantidade")
    @Min(value = 1, message = "Quantidade")
    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao;

    @NotBlank(message = "Responsável")
    @Column(nullable = false)
    private String responsavel;

    @Column(columnDefinition = "Observacao")
    private String observacao;

    @PrePersist
    public void prePersist() {
        this.dataMovimentacao = LocalDateTime.now();
    }
}
