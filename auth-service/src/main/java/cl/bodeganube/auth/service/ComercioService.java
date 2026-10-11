package cl.bodeganube.auth.service;

import cl.bodeganube.auth.dto.ComercioRequest;
import cl.bodeganube.auth.dto.ComercioResponse;
import cl.bodeganube.auth.exception.ConflictException;
import cl.bodeganube.auth.exception.NotFoundException;
import cl.bodeganube.auth.model.Comercio;
import cl.bodeganube.auth.repository.ComercioRepository;
import cl.bodeganube.auth.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComercioService {

    private final ComercioRepository comercioRepo;
    private final UsuarioRepository usuarioRepo;

    @Transactional(readOnly = true)
    public List<ComercioResponse> listar() {
        return comercioRepo.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public ComercioResponse obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public ComercioResponse crear(ComercioRequest r) {
        if (comercioRepo.existsByRut(r.rut())) {
            throw new ConflictException("Ya existe un comercio con el RUT " + r.rut());
        }
        if (comercioRepo.existsByNombre(r.nombre())) {
            throw new ConflictException("Ya existe un comercio con el nombre " + r.nombre());
        }
        Comercio c = new Comercio();
        c.setNombre(r.nombre());
        c.setRut(r.rut());
        return aDto(comercioRepo.save(c));
    }

    @Transactional
    public ComercioResponse actualizar(Long id, ComercioRequest r) {
        Comercio c = buscar(id);
        if (!c.getRut().equals(r.rut()) && comercioRepo.existsByRut(r.rut())) {
            throw new ConflictException("Ya existe un comercio con el RUT " + r.rut());
        }
        if (!c.getNombre().equals(r.nombre()) && comercioRepo.existsByNombre(r.nombre())) {
            throw new ConflictException("Ya existe un comercio con el nombre " + r.nombre());
        }
        c.setNombre(r.nombre());
        c.setRut(r.rut());
        return aDto(comercioRepo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        Comercio c = buscar(id);
        if (usuarioRepo.existsByComercioId(id)) {
            throw new ConflictException("No se puede eliminar: el comercio tiene usuarios asociados");
        }
        comercioRepo.delete(c);
    }

    private Comercio buscar(Long id) {
        return comercioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Comercio " + id + " no encontrado"));
    }

    private ComercioResponse aDto(Comercio c) {
        return new ComercioResponse(c.getId(), c.getNombre(), c.getRut());
    }
}
