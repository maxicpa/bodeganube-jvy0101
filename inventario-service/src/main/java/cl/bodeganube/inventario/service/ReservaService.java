package cl.bodeganube.inventario.service;

import cl.bodeganube.inventario.dto.ItemReservaRequest;
import cl.bodeganube.inventario.dto.ItemReservaResponse;
import cl.bodeganube.inventario.dto.ReservaRequest;
import cl.bodeganube.inventario.dto.ReservaResponse;
import cl.bodeganube.inventario.exception.ConflictException;
import cl.bodeganube.inventario.exception.NotFoundException;
import cl.bodeganube.inventario.model.EstadoReserva;
import cl.bodeganube.inventario.model.Producto;
import cl.bodeganube.inventario.model.Reserva;
import cl.bodeganube.inventario.model.ReservaItem;
import cl.bodeganube.inventario.repository.ProductoRepository;
import cl.bodeganube.inventario.repository.ReservaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de reserva de stock. Todo o nada: si algún SKU no alcanza, no se reserva nada.
 * Es idempotente por ordenId (un reintento devuelve la reserva ya creada).
 */
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ProductoRepository productoRepo;
    private final ReservaRepository reservaRepo;

    @Transactional
    public ReservaResponse reservar(ReservaRequest request) {
        Optional<Reserva> existente = reservaRepo.findByOrdenId(request.ordenId());
        if (existente.isPresent()) {
            return aDto(existente.get());
        }

        // Consolida SKUs repetidos sumando cantidades
        Map<String, Integer> pedido = new TreeMap<>();
        for (ItemReservaRequest item : request.items()) {
            pedido.merge(item.sku(), item.cantidad(), Integer::sum);
        }

        List<Producto> productos = productoRepo.findBySkuInParaActualizar(pedido.keySet());
        Set<String> encontrados = productos.stream().map(Producto::getSku).collect(Collectors.toSet());
        List<String> inexistentes = pedido.keySet().stream().filter(sku -> !encontrados.contains(sku)).toList();
        if (!inexistentes.isEmpty()) {
            throw new NotFoundException("SKU inexistente(s): " + String.join(", ", inexistentes));
        }

        List<String> sinStock = new ArrayList<>();
        for (Producto p : productos) {
            int solicitado = pedido.get(p.getSku());
            if (p.getStockDisponible() < solicitado) {
                sinStock.add(p.getSku() + " (disponible " + p.getStockDisponible() + ", solicitado " + solicitado + ")");
            }
        }
        if (!sinStock.isEmpty()) {
            throw new ConflictException("Stock insuficiente: " + String.join("; ", sinStock));
        }

        Reserva reserva = new Reserva();
        reserva.setOrdenId(request.ordenId());
        reserva.setEstado(EstadoReserva.ACTIVA);
        for (Producto p : productos) {
            int solicitado = pedido.get(p.getSku());
            p.setStockDisponible(p.getStockDisponible() - solicitado);
            p.setStockReservado(p.getStockReservado() + solicitado);
            reserva.agregarItem(p, solicitado);
        }
        return aDto(reservaRepo.save(reserva));
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long ordenId) {
        return aDto(buscar(ordenId));
    }

    /** Descuento definitivo del stock (al despachar): el stock reservado deja de existir. */
    @Transactional
    public ReservaResponse confirmar(Long ordenId) {
        Reserva reserva = buscar(ordenId);
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            return aDto(reserva);
        }
        if (reserva.getEstado() == EstadoReserva.LIBERADA) {
            throw new ConflictException("La reserva de la orden " + ordenId + " ya fue liberada");
        }
        for (ReservaItem item : reserva.getItems()) {
            Producto p = item.getProducto();
            p.setStockReservado(p.getStockReservado() - item.getCantidad());
        }
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return aDto(reserva);
    }

    /** Devuelve el stock reservado al disponible (orden eliminada o cancelada). */
    @Transactional
    public ReservaResponse liberar(Long ordenId) {
        Reserva reserva = buscar(ordenId);
        if (reserva.getEstado() == EstadoReserva.LIBERADA) {
            return aDto(reserva);
        }
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            throw new ConflictException("La reserva de la orden " + ordenId + " ya fue confirmada (despachada)");
        }
        for (ReservaItem item : reserva.getItems()) {
            Producto p = item.getProducto();
            p.setStockReservado(p.getStockReservado() - item.getCantidad());
            p.setStockDisponible(p.getStockDisponible() + item.getCantidad());
        }
        reserva.setEstado(EstadoReserva.LIBERADA);
        return aDto(reserva);
    }

    private Reserva buscar(Long ordenId) {
        return reservaRepo.findByOrdenId(ordenId)
                .orElseThrow(() -> new NotFoundException("No existe reserva para la orden " + ordenId));
    }

    private ReservaResponse aDto(Reserva r) {
        List<ItemReservaResponse> items = r.getItems().stream()
                .map(i -> new ItemReservaResponse(i.getProducto().getSku(), i.getCantidad()))
                .toList();
        return new ReservaResponse(r.getOrdenId(), r.getEstado(), r.getCreadaEn(), items);
    }
}
