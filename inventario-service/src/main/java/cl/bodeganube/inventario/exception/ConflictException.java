package cl.bodeganube.inventario.exception;

/** Conflicto con el estado actual del recurso (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
