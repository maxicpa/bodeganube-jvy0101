package cl.bodeganube.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductoUpdateRequest(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion) {
}
