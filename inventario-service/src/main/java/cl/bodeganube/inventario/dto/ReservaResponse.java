package cl.bodeganube.inventario.dto;

import cl.bodeganube.inventario.model.EstadoReserva;
import java.time.Instant;
import java.util.List;

public record ReservaResponse(Long ordenId, EstadoReserva estado, Instant creadaEn, List<ItemReservaResponse> items) {
}
