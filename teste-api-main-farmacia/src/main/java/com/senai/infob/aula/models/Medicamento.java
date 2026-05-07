package com.farmacia.model;

import com.farmacia.model.enums.CondicaoArmazenamento;
import com.farmacia.model.enums.FormaAdministracao;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "medicamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome")
    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "Descricao")
    private String descricao;

    @NotBlank(message = "Lote")
    @Column(nullable = false)
    private String lote;

    @NotNull(message = "Data de validade")
    @Future(message = "Data de validade")
    @Column(nullable = false)
    private LocalDate validade;

    @NotBlank(message = "Dosagem")
    private String dosagem;

    @NotNull(message = "Forma de administração")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaAdministracao formaAdministracao;

    @NotNull(message = "Condição de armazenamento")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicaoArmazenamento condicaoArmazenamento;

    @NotNull(message = "Quantidade em estoque")
    @Min(value = 0, message = "Quantidade")
    @Column(nullable = false)
    private Integer quantidadeEstoque;

    @NotNull(message = "Quantidade mínima")
    @Min(value = 1, message = "Quantidade mínima")
    @Column(nullable = false)
    private Integer quantidadeMinima;

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movimentacao> movimentacoes;
}
