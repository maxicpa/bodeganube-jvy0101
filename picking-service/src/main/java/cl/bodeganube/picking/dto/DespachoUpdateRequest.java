package cl.bodeganube.picking.dto;

import jakarta.validation.constraints.Size;

public record DespachoUpdateRequest(@Size(max = 80) String transportista, @Size(max = 500) String observaciones) {
}
