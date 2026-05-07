package com.farmacia.controller;

import com.farmacia.dto.AlertaDTO;
import com.farmacia.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaDTO>> listarTodos() {
        return ResponseEntity.ok(alertaService.listarTodosAlertas());
    }

    @GetMapping("/resumo")
    public ResponseEntity<AlertaService.ResumoAlertas> resumo() {
        return ResponseEntity.ok(alertaService.resumoAlertas());
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<AlertaDTO>> estoqueBaixo() {
        return ResponseEntity.ok(alertaService.alertasEstoqueBaixo());
    }

    @GetMapping("/vencimento-proximo")
    public ResponseEntity<List<AlertaDTO>> vencimentoProximo() {
        return ResponseEntity.ok(alertaService.alertasVencimentoProximo());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<AlertaDTO>> vencidos() {
        return ResponseEntity.ok(alertaService.alertasVencidos());
    }
}
