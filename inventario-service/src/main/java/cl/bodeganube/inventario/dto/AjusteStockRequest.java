package cl.bodeganube.inventario.dto;

import jakarta.validation.constraints.NotNull;

/** Cantidad a sumar (positiva) o restar (negativa) del stock disponible. */
public record AjusteStockRequest(@NotNull Integer cantidad) {
}
