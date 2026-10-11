package cl.bodeganube.ordenes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ItemVentaRequest(@NotBlank String sku, @Min(1) int cantidad) {
}
