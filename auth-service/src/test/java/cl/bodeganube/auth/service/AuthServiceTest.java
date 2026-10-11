package cl.bodeganube.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cl.bodeganube.auth.dto.LoginRequest;
import cl.bodeganube.auth.dto.TokenResponse;
import cl.bodeganube.auth.model.Rol;
import cl.bodeganube.auth.model.Usuario;
import cl.bodeganube.auth.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarioRepo;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @InjectMocks
    AuthService authService;

    private Usuario operario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("operario@bodeganube.cl");
        u.setPasswordHash("hash");
        u.setRol(Rol.OPERARIO);
        u.setActivo(true);
        return u;
    }

    @Test
    void login_conCredencialesValidas_devuelveToken() {
        Usuario u = operario();
        when(usuarioRepo.findByEmail("operario@bodeganube.cl")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Operario123", "hash")).thenReturn(true);
        when(jwtService.emitir(u)).thenReturn(new TokenResponse("token-falso", "Bearer", 7200, Rol.OPERARIO, null));

        TokenResponse r = authService.login(new LoginRequest("operario@bodeganube.cl", "Operario123"));

        assertEquals("token-falso", r.accessToken());
        assertEquals(Rol.OPERARIO, r.rol());
    }

    @Test
    void login_conPasswordIncorrecta_lanzaBadCredentials() {
        when(usuarioRepo.findByEmail("operario@bodeganube.cl")).thenReturn(Optional.of(operario()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("operario@bodeganube.cl", "incorrecta")));
    }
}
