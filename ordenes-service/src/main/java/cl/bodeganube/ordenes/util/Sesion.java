package cl.bodeganube.ordenes.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class Sesion {

    private Sesion() {
    }

    /** Si el usuario es un COMERCIO devuelve su comercioId (para filtrar); si es OPERARIO devuelve null. */
    public static Long comercioIdSiEsComercio(Jwt jwt) {
        if (!"COMERCIO".equals(jwt.getClaimAsString("rol"))) {
            return null;
        }
        Number comercioId = jwt.getClaim("comercioId");
        if (comercioId == null) {
            throw new AccessDeniedException("El token no incluye comercioId");
        }
        return comercioId.longValue();
    }
}
