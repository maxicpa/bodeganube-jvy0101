package cl.bodeganube.ordenes.controller;

import static cl.bodeganube.ordenes.config.InternalKeyGuard.HEADER;

import cl.bodeganube.ordenes.config.InternalKeyGuard;
import cl.bodeganube.ordenes.dto.EstadoInternoRequest;
import cl.bodeganube.ordenes.dto.OrdenResponse;
import cl.bodeganube.ordenes.service.OrdenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints privados para Picking y Despacho (cabecera X-Internal-Key). */
@RestController
@RequestMapping("/internal/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes (interno)")
public class OrdenInternaController {

    private final OrdenService service;
    private final InternalKeyGuard guard;

    @GetMapping("/{id}")
    public OrdenResponse obtener(@RequestHeader(value = HEADER, required = false) String clave,
                                 @PathVariable Long id) {
        guard.verificar(clave);
        return service.obtener(id, null);
    }

    @PutMapping("/{id}/estado")
    public OrdenResponse cambiarEstado(@RequestHeader(value = HEADER, required = false) String clave,
                                       @PathVariable Long id,
                                       @Valid @RequestBody EstadoInternoRequest request) {
        guard.verificar(clave);
        return service.cambiarEstadoInterno(id, request);
    }
}
