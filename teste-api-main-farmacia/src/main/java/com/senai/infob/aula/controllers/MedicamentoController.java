package com.farmacia.controller;

import com.farmacia.dto.MedicamentoDTO;
import com.farmacia.model.Medicamento;
import com.farmacia.model.enums.CondicaoArmazenamento;
import com.farmacia.model.enums.FormaAdministracao;
import com.farmacia.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    public ResponseEntity<List<Medicamento>> listarTodos() {
        return ResponseEntity.ok(medicamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicamento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Medicamento> cadastrar(@Valid @RequestBody MedicamentoDTO dto) {
        Medicamento salvo = medicamentoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentoDTO dto) {
        return ResponseEntity.ok(medicamentoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        medicamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Medicamento>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(medicamentoService.buscarPorNome(nome));
    }

    @GetMapping("/buscar/lote")
    public ResponseEntity<List<Medicamento>> buscarPorLote(@RequestParam String lote) {
        return ResponseEntity.ok(medicamentoService.buscarPorLote(lote));
    }

    @GetMapping("/buscar/forma")
    public ResponseEntity<List<Medicamento>> buscarPorForma(
            @RequestParam FormaAdministracao forma) {
        return ResponseEntity.ok(medicamentoService.buscarPorFormaAdministracao(forma));
    }

    @GetMapping("/buscar/condicao")
    public ResponseEntity<List<Medicamento>> buscarPorCondicao(
            @RequestParam CondicaoArmazenamento condicao) {
        return ResponseEntity.ok(medicamentoService.buscarPorCondicaoArmazenamento(condicao));
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<Medicamento>> estoqueBaixo() {
        return ResponseEntity.ok(medicamentoService.buscarEstoqueAbaixoDoMinimo());
    }

    @GetMapping("/proximos-vencimento")
    public ResponseEntity<List<Medicamento>> proximosVencimento(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(medicamentoService.buscarProximosDoVencimento(dias));
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<Medicamento>> vencidos() {
        return ResponseEntity.ok(medicamentoService.buscarVencidos());
    }
}
