package com.farmacia.controller;

import com.farmacia.dto.MovimentacaoDTO;
import com.farmacia.model.Movimentacao;
import com.farmacia.model.enums.TipoMovimentacao;
import com.farmacia.service.MovimentacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping
    public ResponseEntity<List<Movimentacao>> listarTodas() {
        return ResponseEntity.ok(movimentacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movimentacao> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Movimentacao> registrar(@Valid @RequestBody MovimentacaoDTO dto) {
        Movimentacao salva = movimentacaoService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/medicamento/{id}")
    public ResponseEntity<List<Movimentacao>> historicoPorMedicamento(@PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoService.buscarPorMedicamento(id));
    }

    @GetMapping("/tipo")
    public ResponseEntity<List<Movimentacao>> buscarPorTipo(@RequestParam TipoMovimentacao tipo) {
        return ResponseEntity.ok(movimentacaoService.buscarPorTipo(tipo));
    }

    @GetMapping("/responsavel")
    public ResponseEntity<List<Movimentacao>> buscarPorResponsavel(@RequestParam String nome) {
        return ResponseEntity.ok(movimentacaoService.buscarPorResponsavel(nome));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<Movimentacao>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(movimentacaoService.buscarPorPeriodo(inicio, fim));
    }

    @GetMapping("/medicamento/{id}/tipo")
    public ResponseEntity<List<Movimentacao>> buscarPorMedicamentoETipo(
            @PathVariable Long id,
            @RequestParam TipoMovimentacao tipo) {
        return ResponseEntity.ok(movimentacaoService.buscarPorMedicamentoETipo(id, tipo));
    }
}
