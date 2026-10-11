package cl.bodeganube.ordenes.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Protege los endpoints /internal/** (solo para otros microservicios, red privada). */
@Component
public class InternalKeyGuard {

    public static final String HEADER = "X-Internal-Key";

    private final byte[] clave;

    public InternalKeyGuard(@Value("${app.internal-key}") String clave) {
        this.clave = clave.getBytes(StandardCharsets.UTF_8);
    }

    public void verificar(String recibida) {
        if (recibida == null || !MessageDigest.isEqual(clave, recibida.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clave interna inválida");
        }
    }
}
