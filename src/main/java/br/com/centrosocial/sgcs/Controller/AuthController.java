package br.com.centrosocial.sgcs.Controller;

import br.com.centrosocial.sgcs.DTO.Auth.LoginRequest;
import br.com.centrosocial.sgcs.DTO.Auth.LoginResponse;
import br.com.centrosocial.sgcs.DTO.Auth.SetupRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/setup")
    public ResponseEntity<PessoaResponse> configurarAdministradorInicial(@Valid @RequestBody SetupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.configurarAdministradorInicial(request));
    }
}
