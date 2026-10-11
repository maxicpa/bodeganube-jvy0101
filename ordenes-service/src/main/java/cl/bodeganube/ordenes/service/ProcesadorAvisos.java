package cl.bodeganube.ordenes.service;

import cl.bodeganube.ordenes.dto.OrdenResponse;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.model.AvisoWebhook;
import cl.bodeganube.ordenes.model.EstadoAviso;
import cl.bodeganube.ordenes.repository.AvisoWebhookRepository;
import cl.bodeganube.ordenes.util.Texto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Procesamiento asíncrono del aviso (equivale a SQS + Lambda del diseño):
 * crea la orden, controla duplicados y reserva stock fuera del ciclo de la petición HTTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesadorAvisos {

    private final AvisoWebhookRepository avisoRepo;
    private final OrdenService ordenService;

    @Async
    public void procesar(Long avisoId, VentaRequest req) {
        AvisoWebhook aviso = avisoRepo.findById(avisoId).orElseThrow();
        try {
            OrdenResponse orden = ordenService.crear(req);
            aviso.setEstado(EstadoAviso.PROCESADO);
            aviso.setDetalle("Orden " + orden.id() + " con estado " + orden.estado());
        } catch (Exception e) {
            log.error("Error procesando el aviso {}", req.externalId(), e);
            aviso.setEstado(EstadoAviso.ERROR);
            aviso.setDetalle(Texto.truncar(e.getMessage(), 500));
        }
        avisoRepo.save(aviso);
    }
}
