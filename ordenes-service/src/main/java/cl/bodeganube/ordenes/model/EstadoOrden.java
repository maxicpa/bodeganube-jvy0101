package cl.bodeganube.ordenes.model;

public enum EstadoOrden {
    /** Orden creada; la reserva de stock aún no se completa (p. ej. Inventario no respondió). */
    PENDIENTE_RESERVA,
    /** Inventario no tiene stock suficiente: NO está disponible para picking. */
    SIN_STOCK,
    /** Stock reservado: el operario puede hacer el picking. */
    DISPONIBLE_PICKING,
    EN_PICKING,
    DESPACHADA
}
