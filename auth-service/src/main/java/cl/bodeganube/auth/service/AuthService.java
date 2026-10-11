package cl.bodeganube.auth.service;

import cl.bodeganube.auth.dto.LoginRequest;
import cl.bodeganube.auth.dto.TokenResponse;
import cl.bodeganube.auth.model.Usuario;
import cl.bodeganube.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepo.findByEmail(request.email().toLowerCase())
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        return jwtService.emitir(usuario);
    }
}
