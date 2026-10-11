package cl.bodeganube.auth.service;

import cl.bodeganube.auth.dto.TokenResponse;
import cl.bodeganube.auth.model.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final long minutosExpiracion;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes}") long minutosExpiracion) {
        this.encoder = encoder;
        this.minutosExpiracion = minutosExpiracion;
    }

    public TokenResponse emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Long comercioId = usuario.getComercio() == null ? null : usuario.getComercio().getId();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("bodeganube-auth")
                .subject(usuario.getEmail())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES))
                .claim("rol", usuario.getRol().name())
                .claim("uid", usuario.getId());
        if (comercioId != null) {
            claims.claim("comercioId", comercioId);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new TokenResponse(token, "Bearer", minutosExpiracion * 60, usuario.getRol(), comercioId);
    }
}
