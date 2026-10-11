package cl.bodeganube.ordenes.service;

import cl.bodeganube.ordenes.dto.AvisoResponse;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.dto.WebhookAck;
import cl.bodeganube.ordenes.model.AvisoWebhook;
import cl.bodeganube.ordenes.model.EstadoAviso;
import cl.bodeganube.ordenes.repository.AvisoWebhookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final AvisoWebhookRepository avisoRepo;
    private final ProcesadorAvisos procesador;

    /** Registra el aviso, dispara el procesamiento asíncrono y responde de inmediato. */
    public WebhookAck recibir(VentaRequest req) {
        if (avisoRepo.existsByExternalId(req.externalId())) {
            return new WebhookAck(req.externalId(), "DUPLICADO");
        }
        AvisoWebhook aviso = new AvisoWebhook();
        aviso.setExternalId(req.externalId());
        aviso.setComercioId(req.comercioId());
        aviso.setEstado(EstadoAviso.RECIBIDO);
        try {
            aviso = avisoRepo.saveAndFlush(aviso);
        } catch (DataIntegrityViolationException e) {
            return new WebhookAck(req.externalId(), "DUPLICADO");
        }
        procesador.procesar(aviso.getId(), req);
        return new WebhookAck(req.externalId(), "RECIBIDO");
    }

    public List<AvisoResponse> listarAvisos() {
        return avisoRepo.findAllByOrderByIdDesc().stream()
                .map(a -> new AvisoResponse(a.getId(), a.getExternalId(), a.getComercioId(), a.getEstado(),
                        a.getDetalle(), a.getRecibidoEn()))
                .toList();
    }
}
