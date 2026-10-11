package cl.bodeganube.inventario.controller;

import static cl.bodeganube.inventario.config.InternalKeyGuard.HEADER;

import cl.bodeganube.inventario.config.InternalKeyGuard;
import cl.bodeganube.inventario.dto.ReservaRequest;
import cl.bodeganube.inventario.dto.ReservaResponse;
import cl.bodeganube.inventario.service.ReservaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints privados: solo los consumen otros microservicios (cabecera X-Internal-Key). */
@RestController
@RequestMapping("/internal/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas (interno)")
public class ReservaInternaController {

    private final ReservaService service;
    private final InternalKeyGuard guard;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse reservar(@RequestHeader(value = HEADER, required = false) String clave,
                                    @Valid @RequestBody ReservaRequest request) {
        guard.verificar(clave);
        return service.reservar(request);
    }

    @GetMapping("/{ordenId}")
    public ReservaResponse obtener(@RequestHeader(value = HEADER, required = false) String clave,
                                   @PathVariable Long ordenId) {
        guard.verificar(clave);
        return service.obtener(ordenId);
    }

    @PostMapping("/{ordenId}/confirmar")
    public ReservaResponse confirmar(@RequestHeader(value = HEADER, required = false) String clave,
                                     @PathVariable Long ordenId) {
        guard.verificar(clave);
        return service.confirmar(ordenId);
    }

    @PostMapping("/{ordenId}/liberar")
    public ReservaResponse liberar(@RequestHeader(value = HEADER, required = false) String clave,
                                   @PathVariable Long ordenId) {
        guard.verificar(clave);
        return service.liberar(ordenId);
    }
}
