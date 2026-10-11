package cl.bodeganube.ordenes.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Traduce errores de llamadas entre microservicios a excepciones propias. */
public final class ErrorRemoto {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ErrorRemoto() {
    }

    /** Extrae el campo "detail" del ProblemDetail devuelto por el otro servicio. */
    public static String detalle(RestClientResponseException e) {
        try {
            JsonNode nodo = MAPPER.readTree(e.getResponseBodyAsString());
            String detalle = nodo.path("detail").asText("");
            if (!detalle.isBlank()) {
                return detalle;
            }
        } catch (Exception ignorada) {
            // se usa el mensaje genérico
        }
        return "Error remoto HTTP " + e.getStatusCode().value();
    }

    public static RuntimeException traducir(String servicio, RestClientException e) {
        if (e instanceof HttpClientErrorException h) {
            int status = h.getStatusCode().value();
            if (status == 404) {
                return new NotFoundException(detalle(h));
            }
            if (status == 409) {
                return new ConflictException(detalle(h));
            }
        }
        return new ServicioNoDisponibleException(servicio + " no disponible: " + e.getMessage());
    }
}
