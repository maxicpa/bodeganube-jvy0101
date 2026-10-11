package cl.bodeganube.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductoCreateRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @Min(0) int stockInicial) {
}
