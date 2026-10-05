package br.com.centrosocial.sgcs.Controller;

import br.com.centrosocial.sgcs.DTO.Familia.*;
import br.com.centrosocial.sgcs.Service.FamiliaService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {
    private final FamiliaService familiaService;
    public FamiliaController(FamiliaService familiaService) { this.familiaService = familiaService; }

    @GetMapping
    public List<FamiliaResumoResponse> listar(@RequestParam(required = false) String busca,
                                               @RequestParam(required = false) Boolean status) {
        return familiaService.listar(busca, status);
    }
    @GetMapping("/{id}") public FamiliaResponse buscar(@PathVariable Long id) { return familiaService.buscarPorId(id); }
    @PostMapping public ResponseEntity<FamiliaResponse> cadastrar(@Valid @RequestBody FamiliaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familiaService.cadastrar(request));
    }
    @PutMapping("/{id}") public FamiliaResponse atualizar(@PathVariable Long id, @Valid @RequestBody FamiliaRequest request) {
        return familiaService.atualizar(id, request);
    }
    @PatchMapping("/{id}/inativar") public ResponseEntity<Void> inativar(@PathVariable Long id) {
        familiaService.inativar(id); return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/reativar") public ResponseEntity<Void> reativar(@PathVariable Long id) {
        familiaService.reativar(id); return ResponseEntity.noContent().build();
    }
}
