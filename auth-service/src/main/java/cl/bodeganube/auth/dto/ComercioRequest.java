package cl.bodeganube.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComercioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 20) String rut) {
}
