package cl.bodeganube.auth.controller;

import cl.bodeganube.auth.dto.LoginRequest;
import cl.bodeganube.auth.dto.TokenResponse;
import cl.bodeganube.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un token JWT con el rol del usuario")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve la identidad contenida en el token")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("email", jwt.getSubject(), "rol", jwt.getClaimAsString("rol"));
    }
}
