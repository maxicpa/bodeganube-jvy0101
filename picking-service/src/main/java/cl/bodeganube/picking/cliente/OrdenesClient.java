package cl.bodeganube.picking.cliente;

import cl.bodeganube.picking.exception.ErrorRemoto;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST del microservicio de Órdenes (consulta y cambio de estado). */
@Component
public class OrdenesClient {

    public record ItemRemoto(String sku, int cantidad) {
    }

    public record OrdenRemota(Long id, Long comercioId, String estado, List<ItemRemoto> items) {
    }

    public record CambioEstado(String estado, String trackingNumber) {
    }

    private final RestClient restClient;

    public OrdenesClient(@Qualifier("ordenesRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public OrdenRemota obtener(Long ordenId) {
        try {
            return restClient.get().uri("/internal/ordenes/{id}", ordenId)
                    .retrieve()
                    .body(OrdenRemota.class);
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir("Órdenes", e);
        }
    }

    public void cambiarEstado(Long ordenId, String estado, String trackingNumber) {
        try {
            restClient.put().uri("/internal/ordenes/{id}/estado", ordenId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CambioEstado(estado, trackingNumber))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir("Órdenes", e);
        }
    }
}
