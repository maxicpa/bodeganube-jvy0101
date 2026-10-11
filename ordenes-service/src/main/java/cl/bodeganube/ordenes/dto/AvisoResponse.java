package cl.bodeganube.ordenes.dto;

import cl.bodeganube.ordenes.model.EstadoAviso;
import java.time.Instant;

public record AvisoResponse(Long id, String externalId, Long comercioId, EstadoAviso estado, String detalle,
                            Instant recibidoEn) {
}
