package cl.bodeganube.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReservaRequest(@NotNull Long ordenId, @NotEmpty @Valid List<ItemReservaRequest> items) {
}
