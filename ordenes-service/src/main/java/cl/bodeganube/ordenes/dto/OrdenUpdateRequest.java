package cl.bodeganube.ordenes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrdenUpdateRequest(@NotBlank @Size(max = 150) String cliente) {
}
