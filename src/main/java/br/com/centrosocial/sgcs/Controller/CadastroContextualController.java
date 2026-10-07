package br.com.centrosocial.sgcs.Controller;

import br.com.centrosocial.sgcs.DTO.Cadastro.CadastroContextualRequest;
import br.com.centrosocial.sgcs.DTO.Cadastro.CadastroContextualResponse;
import br.com.centrosocial.sgcs.Service.CadastroContextualService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cadastros/contextuais")
public class CadastroContextualController {
    private final CadastroContextualService cadastroContextualService;

    public CadastroContextualController(CadastroContextualService cadastroContextualService) {
        this.cadastroContextualService = cadastroContextualService;
    }

    @PostMapping
    public ResponseEntity<CadastroContextualResponse> cadastrar(
            @Valid @RequestBody CadastroContextualRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroContextualService.cadastrar(request));
    }

    @PutMapping("/{pessoaId}")
    public CadastroContextualResponse atualizar(@PathVariable Long pessoaId,
                                                 @Valid @RequestBody CadastroContextualRequest request) {
        return cadastroContextualService.atualizar(pessoaId, request);
    }
}
