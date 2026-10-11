package cl.bodeganube.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ItemReservaRequest(@NotBlank String sku, @Min(1) int cantidad) {
}
