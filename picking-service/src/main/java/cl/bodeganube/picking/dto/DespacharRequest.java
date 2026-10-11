package cl.bodeganube.picking.dto;

import jakarta.validation.constraints.Size;

public record DespacharRequest(@Size(max = 80) String transportista) {
}
