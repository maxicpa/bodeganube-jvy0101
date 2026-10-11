package cl.bodeganube.picking.cliente;

import cl.bodeganube.picking.exception.ErrorRemoto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST del microservicio de Inventario (descuento definitivo del stock). */
@Component
public class InventarioClient {

    private final RestClient restClient;

    public InventarioClient(@Qualifier("inventarioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void confirmar(Long ordenId) {
        try {
            restClient.post().uri("/internal/reservas/{ordenId}/confirmar", ordenId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir("Inventario", e);
        }
    }
}
