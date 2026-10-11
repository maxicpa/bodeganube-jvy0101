package cl.bodeganube.auth.controller;

import cl.bodeganube.auth.dto.ComercioRequest;
import cl.bodeganube.auth.dto.ComercioResponse;
import cl.bodeganube.auth.service.ComercioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OPERARIO')")
@Tag(name = "Comercios")
public class ComercioController {

    private final ComercioService service;

    @GetMapping
    public List<ComercioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ComercioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComercioResponse crear(@Valid @RequestBody ComercioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ComercioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ComercioRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
