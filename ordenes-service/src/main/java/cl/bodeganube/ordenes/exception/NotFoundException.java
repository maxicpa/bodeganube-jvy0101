package cl.bodeganube.ordenes.exception;

/** Recurso no encontrado (HTTP 404). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}
