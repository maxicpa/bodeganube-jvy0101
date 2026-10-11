package cl.bodeganube.auth.exception;

/** Regla de negocio incumplida por la solicitud (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String mensaje) {
        super(mensaje);
    }
}
