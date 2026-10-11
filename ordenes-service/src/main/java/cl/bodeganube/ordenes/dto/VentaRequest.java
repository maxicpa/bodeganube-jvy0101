package cl.bodeganube.ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Aviso de venta (webhook) o creación manual de una orden. */
public record VentaRequest(
        @NotBlank @Size(max = 100) String externalId,
        @NotNull Long comercioId,
        @NotBlank @Size(max = 150) String cliente,
        @NotEmpty @Valid List<ItemVentaRequest> items) {
}
