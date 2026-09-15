package br.com.centrosocial.sgcs.Service;

import br.com.centrosocial.sgcs.DTO.Auth.LoginRequest;
import br.com.centrosocial.sgcs.DTO.Auth.LoginResponse;
import br.com.centrosocial.sgcs.DTO.Auth.SetupRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.FisicaRequest;
import br.com.centrosocial.sgcs.DTO.Pessoa.PessoaResponse;
import br.com.centrosocial.sgcs.Exception.ConflictException;
import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import br.com.centrosocial.sgcs.Models.Pessoa.Perfil;
import br.com.centrosocial.sgcs.Repository.FisicaRepository;
import br.com.centrosocial.sgcs.Security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final FisicaRepository fisicaRepository;
    private final JwtService jwtService;
    private final PessoaService pessoaService;

    public AuthService(
            AuthenticationManager authenticationManager,
            FisicaRepository fisicaRepository,
            JwtService jwtService,
            PessoaService pessoaService
    ) {
        this.authenticationManager = authenticationManager;
        this.fisicaRepository = fisicaRepository;
        this.jwtService = jwtService;
        this.pessoaService = pessoaService;
    }

    public synchronized PessoaResponse configurarAdministradorInicial(SetupRequest request) {
        if (fisicaRepository.count() > 0) {
            throw new ConflictException("Configuração inicial já realizada.");
        }

        FisicaRequest administrador = new FisicaRequest(
                request.telefone(), request.cep(), request.logradouro(), request.numero(), request.bairro(),
                request.cidade(), request.estado(), request.nome(), request.cpf(), request.dataNascimento(),
                request.email(), request.usuario(), request.senha(), Perfil.ADMINISTRADOR
        );

        return pessoaService.cadastrarFisica(administrador);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.senha())
        );

        Fisica fisica = fisicaRepository.findByUsuarioAndStatusTrue(request.usuario())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        return new LoginResponse(
                jwtService.gerarToken(fisica)
        );
    }
}
