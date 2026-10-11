package cl.bodeganube.ordenes.dto;

import cl.bodeganube.ordenes.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;

/** Cambio de estado solicitado por Picking y Despacho (llamada interna). */
public record EstadoInternoRequest(@NotNull EstadoOrden estado, String trackingNumber) {
}
