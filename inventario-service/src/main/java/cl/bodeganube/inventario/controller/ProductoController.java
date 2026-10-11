package cl.bodeganube.inventario.controller;

import cl.bodeganube.inventario.dto.AjusteStockRequest;
import cl.bodeganube.inventario.dto.ProductoCreateRequest;
import cl.bodeganube.inventario.dto.ProductoResponse;
import cl.bodeganube.inventario.dto.ProductoUpdateRequest;
import cl.bodeganube.inventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Catálogo y stock")
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    @Operation(summary = "Lista el catálogo con su stock (cualquier usuario autenticado)")
    public List<ProductoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERARIO')")
    public ProductoResponse crear(@Valid @RequestBody ProductoCreateRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERARIO')")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @PostMapping("/{id}/ajuste-stock")
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Suma o resta unidades al stock disponible")
    public ProductoResponse ajustarStock(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest request) {
        return service.ajustarStock(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERARIO')")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
