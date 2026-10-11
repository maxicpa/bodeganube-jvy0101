package cl.bodeganube.ordenes.service;

import cl.bodeganube.ordenes.cliente.InventarioClient;
import cl.bodeganube.ordenes.cliente.InventarioClient.ItemRemoto;
import cl.bodeganube.ordenes.cliente.InventarioClient.ResultadoReserva;
import cl.bodeganube.ordenes.dto.EstadoInternoRequest;
import cl.bodeganube.ordenes.dto.ItemOrdenResponse;
import cl.bodeganube.ordenes.dto.ItemVentaRequest;
import cl.bodeganube.ordenes.dto.OrdenResponse;
import cl.bodeganube.ordenes.dto.OrdenUpdateRequest;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.exception.BadRequestException;
import cl.bodeganube.ordenes.exception.ConflictException;
import cl.bodeganube.ordenes.exception.NotFoundException;
import cl.bodeganube.ordenes.model.EstadoOrden;
import cl.bodeganube.ordenes.model.Orden;
import cl.bodeganube.ordenes.repository.OrdenRepository;
import cl.bodeganube.ordenes.util.Texto;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepo;
    private final InventarioClient inventario;

    /**
     * Crea la orden de forma idempotente (por externalId) y reserva stock.
     * No es @Transactional a propósito: la llamada HTTP a Inventario no debe mantener
     * una transacción de base de datos abierta.
     */
    public OrdenResponse crear(VentaRequest req) {
        Optional<Orden> existente = ordenRepo.findWithItemsByExternalId(req.externalId());
        if (existente.isPresent()) {
            return aDto(existente.get());
        }

        Orden nueva = new Orden();
        nueva.setExternalId(req.externalId());
        nueva.setComercioId(req.comercioId());
        nueva.setCliente(req.cliente());
        nueva.setEstado(EstadoOrden.PENDIENTE_RESERVA);
        for (ItemVentaRequest item : req.items()) {
            nueva.agregarItem(item.sku(), item.cantidad());
        }

        Orden guardada;
        try {
            guardada = ordenRepo.save(nueva);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ya existe una orden con externalId " + req.externalId());
        }
        return aDto(reservarYActualizar(guardada));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar(Long comercioId, EstadoOrden estado) {
        List<Orden> ordenes;
        if (comercioId != null && estado != null) {
            ordenes = ordenRepo.findByComercioIdAndEstadoOrderByIdDesc(comercioId, estado);
        } else if (comercioId != null) {
            ordenes = ordenRepo.findByComercioIdOrderByIdDesc(comercioId);
        } else if (estado != null) {
            ordenes = ordenRepo.findByEstadoOrderByIdDesc(estado);
        } else {
            ordenes = ordenRepo.findAllByOrderByIdDesc();
        }
        return ordenes.stream().map(this::aDto).toList();
    }

    /** Órdenes con stock reservado, las más antiguas primero. */
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarParaPicking() {
        return ordenRepo.findByEstadoOrderByIdAsc(EstadoOrden.DISPONIBLE_PICKING).stream().map(this::aDto).toList();
    }

    /** comercioId != null restringe el acceso: un comercio solo ve sus propias órdenes. */
    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id, Long comercioId) {
        Orden orden = buscar(id);
        if (comercioId != null && !comercioId.equals(orden.getComercioId())) {
            throw new NotFoundException("Orden " + id + " no encontrada");
        }
        return aDto(orden);
    }

    @Transactional
    public OrdenResponse actualizar(Long id, OrdenUpdateRequest req) {
        Orden orden = buscar(id);
        if (orden.getEstado() == EstadoOrden.EN_PICKING || orden.getEstado() == EstadoOrden.DESPACHADA) {
            throw new ConflictException("No se puede modificar una orden en estado " + orden.getEstado());
        }
        orden.setCliente(req.cliente());
        return aDto(ordenRepo.save(orden));
    }

    /** Reintenta la reserva de una orden SIN_STOCK o PENDIENTE_RESERVA (p. ej. tras reponer stock). */
    public OrdenResponse reintentarReserva(Long id) {
        Orden orden = buscar(id);
        if (orden.getEstado() != EstadoOrden.SIN_STOCK && orden.getEstado() != EstadoOrden.PENDIENTE_RESERVA) {
            throw new ConflictException("Solo se puede reintentar en SIN_STOCK o PENDIENTE_RESERVA (estado actual: "
                    + orden.getEstado() + ")");
        }
        return aDto(reservarYActualizar(orden));
    }

    public void eliminar(Long id) {
        Orden orden = buscar(id);
        if (orden.getEstado() == EstadoOrden.EN_PICKING || orden.getEstado() == EstadoOrden.DESPACHADA) {
            throw new ConflictException("No se puede eliminar una orden en estado " + orden.getEstado());
        }
        if (orden.getEstado() == EstadoOrden.DISPONIBLE_PICKING) {
            inventario.liberar(orden.getId());
        }
        ordenRepo.delete(orden);
    }

    /** Transiciones que solo puede ejecutar Picking y Despacho (vía endpoint interno). */
    @Transactional
    public OrdenResponse cambiarEstadoInterno(Long id, EstadoInternoRequest req) {
        Orden orden = buscar(id);
        EstadoOrden actual = orden.getEstado();
        EstadoOrden nuevo = req.estado();

        // Reintento idempotente: misma transición ya aplicada con el mismo tracking
        if (actual == EstadoOrden.DESPACHADA && nuevo == EstadoOrden.DESPACHADA
                && Objects.equals(orden.getTrackingNumber(), req.trackingNumber())) {
            return aDto(orden);
        }

        boolean valida = (actual == EstadoOrden.DISPONIBLE_PICKING && nuevo == EstadoOrden.EN_PICKING)
                || (actual == EstadoOrden.EN_PICKING && nuevo == EstadoOrden.DISPONIBLE_PICKING)
                || (actual == EstadoOrden.EN_PICKING && nuevo == EstadoOrden.DESPACHADA);
        if (!valida) {
            throw new ConflictException("Transición no permitida: " + actual + " -> " + nuevo);
        }
        if (nuevo == EstadoOrden.DESPACHADA) {
            if (req.trackingNumber() == null || req.trackingNumber().isBlank()) {
                throw new BadRequestException("Para pasar a DESPACHADA se requiere trackingNumber");
            }
            orden.setTrackingNumber(req.trackingNumber());
        }
        orden.setEstado(nuevo);
        return aDto(ordenRepo.save(orden));
    }

    private Orden reservarYActualizar(Orden orden) {
        List<ItemRemoto> items = orden.getItems().stream()
                .map(i -> new ItemRemoto(i.getSku(), i.getCantidad()))
                .toList();
        ResultadoReserva resultado = inventario.reservar(orden.getId(), items);
        if (resultado.aprobada()) {
            orden.setEstado(EstadoOrden.DISPONIBLE_PICKING);
            orden.setMotivo(null);
        } else {
            orden.setEstado(EstadoOrden.SIN_STOCK);
            orden.setMotivo(Texto.truncar(resultado.motivo(), 500));
        }
        return ordenRepo.save(orden);
    }

    private Orden buscar(Long id) {
        return ordenRepo.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Orden " + id + " no encontrada"));
    }

    private OrdenResponse aDto(Orden o) {
        List<ItemOrdenResponse> items = o.getItems().stream()
                .map(i -> new ItemOrdenResponse(i.getSku(), i.getCantidad()))
                .toList();
        return new OrdenResponse(o.getId(), o.getExternalId(), o.getComercioId(), o.getCliente(), o.getEstado(),
                o.getMotivo(), o.getTrackingNumber(), o.getCreadaEn(), o.getActualizadaEn(), items);
    }
}
