package cl.bodeganube.ordenes.controller;

import cl.bodeganube.ordenes.dto.AvisoResponse;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.dto.WebhookAck;
import cl.bodeganube.ordenes.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "Webhook de ventas")
public class WebhookController {

    private final WebhookService service;
    private final byte[] token;

    public WebhookController(WebhookService service, @Value("${app.webhook.token}") String token) {
        this.service = service;
        this.token = token.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/api/webhooks/ventas")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Recibe un aviso de venta y responde 202 de inmediato; la orden se crea de forma asíncrona")
    public WebhookAck recibir(@RequestHeader(value = "X-Webhook-Token", required = false) String recibido,
                              @Valid @RequestBody VentaRequest request) {
        if (recibido == null || !MessageDigest.isEqual(token, recibido.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de webhook inválido");
        }
        return service.recibir(request);
    }

    @GetMapping("/api/avisos")
    @PreAuthorize("hasRole('OPERARIO')")
    @Operation(summary = "Lista los avisos recibidos y el resultado de su procesamiento")
    public List<AvisoResponse> avisos() {
        return service.listarAvisos();
    }
}
