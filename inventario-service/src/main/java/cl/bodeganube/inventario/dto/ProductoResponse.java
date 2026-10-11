package cl.bodeganube.inventario.dto;

public record ProductoResponse(Long id, String sku, String nombre, String descripcion,
                               int stockDisponible, int stockReservado) {
}
