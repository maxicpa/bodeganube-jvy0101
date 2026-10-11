package cl.bodeganube.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.bodeganube.auth.dto.UsuarioCreateRequest;
import cl.bodeganube.auth.exception.BadRequestException;
import cl.bodeganube.auth.exception.ConflictException;
import cl.bodeganube.auth.model.Rol;
import cl.bodeganube.auth.repository.ComercioRepository;
import cl.bodeganube.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepo;
    @Mock
    ComercioRepository comercioRepo;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UsuarioService usuarioService;

    @Test
    void crear_rolComercioSinComercioId_lanzaBadRequest() {
        var request = new UsuarioCreateRequest("Ana", "ana@tienda.cl", "Clave1234", Rol.COMERCIO, null);
        assertThrows(BadRequestException.class, () -> usuarioService.crear(request));
    }

    @Test
    void crear_correoDuplicado_lanzaConflict() {
        Mockito.when(usuarioRepo.existsByEmail("ana@tienda.cl")).thenReturn(true);
        var request = new UsuarioCreateRequest("Ana", "ana@tienda.cl", "Clave1234", Rol.OPERARIO, null);
        assertThrows(ConflictException.class, () -> usuarioService.crear(request));
    }
}
