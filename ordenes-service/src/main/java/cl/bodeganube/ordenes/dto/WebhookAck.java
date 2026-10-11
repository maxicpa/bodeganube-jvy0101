package cl.bodeganube.ordenes.dto;

/** Respuesta inmediata al canal externo: solo confirma la recepción del aviso. */
public record WebhookAck(String externalId, String estado) {
}
