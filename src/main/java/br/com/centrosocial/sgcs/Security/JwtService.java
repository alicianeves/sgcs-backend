package br.com.centrosocial.sgcs.Security;

import br.com.centrosocial.sgcs.Models.Pessoa.Fisica;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration:PT8H}") Duration expiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public String gerarToken(Fisica fisica) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("sgcs")
                .issuedAt(agora)
                .expiresAt(agora.plus(expiration))
                .subject(fisica.getUsuario())
                .claim("pessoaId", fisica.getId())
                .claim("roles", List.of("ROLE_" + fisica.getPerfil().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
