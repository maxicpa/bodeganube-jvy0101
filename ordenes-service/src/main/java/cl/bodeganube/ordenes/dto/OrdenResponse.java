package cl.bodeganube.ordenes.dto;

import cl.bodeganube.ordenes.model.EstadoOrden;
import java.time.Instant;
import java.util.List;

public record OrdenResponse(Long id, String externalId, Long comercioId, String cliente, EstadoOrden estado,
                            String motivo, String trackingNumber, Instant creadaEn, Instant actualizadaEn,
                            List<ItemOrdenResponse> items) {
}
