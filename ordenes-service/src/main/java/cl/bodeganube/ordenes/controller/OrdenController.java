package cl.bodeganube.ordenes.controller;

import cl.bodeganube.ordenes.dto.OrdenResponse;
import cl.bodeganube.ordenes.dto.OrdenUpdateRequest;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.model.EstadoOrden;
import cl.bodeganube.ordenes.service.OrdenService;
import cl.bodeganube.ordenes.util.Sesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes")
public class OrdenController {

    private final OrdenService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERARIO','COMERCIO')")
    @Operation(summary = "Lista órdenes. Un COMERCIO solo ve las suyas; el OPERARIO ve todas (filtro opcional por estado)")
    public List<OrdenResponse> listar(@RequestParam(required = false) EstadoOrden estado,
                                      @AuthenticationPrincipal Jwt jwt) {
        return service.listar(Sesion.comercioIdSiEsComercio(jwt), estado);
    }

    @GetMapping("/picking")
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Órdenes disponibles para picking (stock ya reservado)")
    public List<OrdenResponse> paraPicking() {
        return service.listarParaPicking();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERARIO','COMERCIO')")
    public OrdenResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.obtener(id, Sesion.comercioIdSiEsComercio(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Crea una orden manualmente (misma lógica que el webhook, pero síncrona)")
    public OrdenResponse crear(@Valid @RequestBody VentaRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERARIO')")
    public OrdenResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrdenUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @PostMapping("/{id}/reintentar-reserva")
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Reintenta la reserva de stock de una orden SIN_STOCK (p. ej. tras reponer inventario)")
    public OrdenResponse reintentarReserva(@PathVariable Long id) {
        return service.reintentarReserva(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Elimina una orden que aún no está en picking (libera su reserva si la tenía)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
