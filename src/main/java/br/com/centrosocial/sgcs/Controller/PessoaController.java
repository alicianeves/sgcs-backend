package br.com.centrosocial.sgcs.Controller;

import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.JuridicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {
    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @GetMapping
    public List<PessoaResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "true") boolean status
    ) {
        return pessoaService.listar(busca, status);
    }

    @GetMapping("/{id}")
    public PessoaResponse buscarPorId(@PathVariable Long id) {
        return pessoaService.buscarPorId(id);
    }

    @PostMapping("/fisicas")
    public ResponseEntity<PessoaResponse> cadastrarFisica(@Valid @RequestBody FisicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pessoaService.cadastrarFisica(request));
    }

    @PutMapping("/fisicas/{id}")
    public PessoaResponse atualizarFisica(@PathVariable Long id, @Valid @RequestBody FisicaRequest request) {
        return pessoaService.atualizarFisica(id, request);
    }

    @DeleteMapping("/fisicas/{id}/acesso")
    public ResponseEntity<Void> removerAcesso(@PathVariable Long id) {
        pessoaService.removerAcesso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/juridicas")
    public ResponseEntity<PessoaResponse> cadastrarJuridica(@Valid @RequestBody JuridicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pessoaService.cadastrarJuridica(request));
    }

    @PutMapping("/juridicas/{id}")
    public PessoaResponse atualizarJuridica(@PathVariable Long id, @Valid @RequestBody JuridicaRequest request) {
        return pessoaService.atualizarJuridica(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        pessoaService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        pessoaService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
