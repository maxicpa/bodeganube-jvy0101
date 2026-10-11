package cl.bodeganube.picking.dto;

import cl.bodeganube.picking.model.EstadoDespacho;
import java.time.Instant;
import java.util.List;

public record DespachoResponse(Long id, Long ordenId, Long comercioId, EstadoDespacho estado,
                               String trackingNumber, String transportista, String observaciones,
                               Instant creadoEn, Instant despachadoEn, List<ItemDespachoResponse> items) {
}
