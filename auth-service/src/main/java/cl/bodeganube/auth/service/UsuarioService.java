package cl.bodeganube.auth.service;

import cl.bodeganube.auth.dto.UsuarioCreateRequest;
import cl.bodeganube.auth.dto.UsuarioResponse;
import cl.bodeganube.auth.dto.UsuarioUpdateRequest;
import cl.bodeganube.auth.exception.BadRequestException;
import cl.bodeganube.auth.exception.ConflictException;
import cl.bodeganube.auth.exception.NotFoundException;
import cl.bodeganube.auth.model.Comercio;
import cl.bodeganube.auth.model.Rol;
import cl.bodeganube.auth.model.Usuario;
import cl.bodeganube.auth.repository.ComercioRepository;
import cl.bodeganube.auth.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final ComercioRepository comercioRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepo.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest r) {
        String email = r.email().toLowerCase();
        if (usuarioRepo.existsByEmail(email)) {
            throw new ConflictException("Ya existe un usuario con el correo " + email);
        }

        Comercio comercio = null;
        if (r.rol() == Rol.COMERCIO) {
            if (r.comercioId() == null) {
                throw new BadRequestException("Un usuario con rol COMERCIO requiere comercioId");
            }
            comercio = comercioRepo.findById(r.comercioId())
                    .orElseThrow(() -> new NotFoundException("Comercio " + r.comercioId() + " no encontrado"));
        } else if (r.comercioId() != null) {
            throw new BadRequestException("Un OPERARIO no debe asociarse a un comercio");
        }

        Usuario u = new Usuario();
        u.setNombre(r.nombre());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(r.password()));
        u.setRol(r.rol());
        u.setComercio(comercio);
        return aDto(usuarioRepo.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest r) {
        Usuario u = buscar(id);
        u.setNombre(r.nombre());
        if (r.activo() != null) {
            u.setActivo(r.activo());
        }
        if (r.password() != null && !r.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(r.password()));
        }
        return aDto(usuarioRepo.save(u));
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepo.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario " + id + " no encontrado"));
    }

    private UsuarioResponse aDto(Usuario u) {
        Long comercioId = u.getComercio() == null ? null : u.getComercio().getId();
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo(), comercioId);
    }
}
