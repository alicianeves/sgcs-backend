package br.com.centrosocial.sgcs.Controller;

import br.com.centrosocial.sgcs.DTO.Atendimento.*;
import br.com.centrosocial.sgcs.Service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {
    private final AtendimentoService atendimentoService;
    public AtendimentoController(AtendimentoService atendimentoService) { this.atendimentoService = atendimentoService; }
    @GetMapping public List<AtendimentoResponse> listar(@RequestParam(required = false) Long fisicaId) { return atendimentoService.listar(fisicaId); }
    @GetMapping("/{id}") public AtendimentoResponse buscar(@PathVariable Long id) { return atendimentoService.buscarPorId(id); }
    @PostMapping public ResponseEntity<AtendimentoResponse> cadastrar(@Valid @RequestBody AtendimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoService.cadastrar(request));
    }
    @PutMapping("/{id}") public AtendimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtendimentoRequest request) { return atendimentoService.atualizar(id, request); }
    @PatchMapping("/{id}/inativar") public ResponseEntity<Void> inativar(@PathVariable Long id) { atendimentoService.inativar(id); return ResponseEntity.noContent().build(); }
    @PatchMapping("/{id}/reativar") public ResponseEntity<Void> reativar(@PathVariable Long id) { atendimentoService.reativar(id); return ResponseEntity.noContent().build(); }
}
