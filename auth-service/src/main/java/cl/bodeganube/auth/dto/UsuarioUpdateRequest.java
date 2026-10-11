package cl.bodeganube.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** La contraseña es opcional: solo se cambia si viene informada. */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 120) String nombre,
        Boolean activo,
        @Size(min = 8, max = 72) String password) {
}
