package cl.bodeganube.picking.controller;

import cl.bodeganube.picking.dto.DespachoResponse;
import cl.bodeganube.picking.dto.DespachoUpdateRequest;
import cl.bodeganube.picking.dto.DespacharRequest;
import cl.bodeganube.picking.dto.IniciarPickingRequest;
import cl.bodeganube.picking.model.EstadoDespacho;
import cl.bodeganube.picking.service.DespachoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OPERARIO')")
@Tag(name = "Picking y despacho")
public class DespachoController {

    private final DespachoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia el picking de una orden DISPONIBLE_PICKING (la pasa a EN_PICKING)")
    public DespachoResponse iniciar(@Valid @RequestBody IniciarPickingRequest request) {
        return service.iniciar(request.ordenId());
    }

    @GetMapping
    public List<DespachoResponse> listar(@RequestParam(required = false) EstadoDespacho estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public DespachoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/orden/{ordenId}")
    public DespachoResponse obtenerPorOrden(@PathVariable Long ordenId) {
        return service.obtenerPorOrden(ordenId);
    }

    @PutMapping("/{id}")
    public DespachoResponse actualizar(@PathVariable Long id, @Valid @RequestBody DespachoUpdateRequest request) {
        return service.actualizar(id, request);
    }

    @PostMapping("/{id}/despachar")
    @Operation(summary = "Registra el despacho: genera el tracking, descuenta stock y marca la orden DESPACHADA")
    public DespachoResponse despachar(@PathVariable Long id,
                                      @Valid @RequestBody(required = false) DespacharRequest request) {
        return service.despachar(id, request == null ? null : request.transportista());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancela un picking en curso (la orden vuelve a DISPONIBLE_PICKING)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
