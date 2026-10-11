package cl.bodeganube.picking.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarPickingRequest(@NotNull Long ordenId) {
}
