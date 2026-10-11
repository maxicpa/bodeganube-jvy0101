package cl.bodeganube.ordenes.cliente;

import cl.bodeganube.ordenes.exception.ErrorRemoto;
import cl.bodeganube.ordenes.exception.ServicioNoDisponibleException;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente REST del microservicio de Inventario (reserva y liberación de stock). */
@Component
public class InventarioClient {

    public record ItemRemoto(String sku, int cantidad) {
    }

    public record ReservaRemotaRequest(Long ordenId, List<ItemRemoto> items) {
    }

    /** aprobada=false significa "sin stock" o "SKU inexistente" (motivo explica la causa). */
    public record ResultadoReserva(boolean aprobada, String motivo) {
    }

    private final RestClient restClient;

    public InventarioClient(@Qualifier("inventarioRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ResultadoReserva reservar(Long ordenId, List<ItemRemoto> items) {
        try {
            restClient.post().uri("/internal/reservas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReservaRemotaRequest(ordenId, items))
                    .retrieve()
                    .toBodilessEntity();
            return new ResultadoReserva(true, null);
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 404 || status == 409) {
                return new ResultadoReserva(false, ErrorRemoto.detalle(e));
            }
            throw new ServicioNoDisponibleException("Inventario respondió HTTP " + status);
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("Inventario no disponible: " + e.getMessage());
        }
    }

    public void liberar(Long ordenId) {
        try {
            restClient.post().uri("/internal/reservas/{ordenId}/liberar", ordenId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw ErrorRemoto.traducir("Inventario", e);
        }
    }
}
