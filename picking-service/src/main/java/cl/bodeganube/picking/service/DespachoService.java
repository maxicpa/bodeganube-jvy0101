package cl.bodeganube.picking.service;

import cl.bodeganube.picking.cliente.InventarioClient;
import cl.bodeganube.picking.cliente.OrdenesClient;
import cl.bodeganube.picking.cliente.OrdenesClient.ItemRemoto;
import cl.bodeganube.picking.cliente.OrdenesClient.OrdenRemota;
import cl.bodeganube.picking.dto.DespachoResponse;
import cl.bodeganube.picking.dto.DespachoUpdateRequest;
import cl.bodeganube.picking.dto.ItemDespachoResponse;
import cl.bodeganube.picking.exception.ConflictException;
import cl.bodeganube.picking.exception.NotFoundException;
import cl.bodeganube.picking.model.Despacho;
import cl.bodeganube.picking.model.EstadoDespacho;
import cl.bodeganube.picking.repository.DespachoRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Flujo del operario: iniciar picking -> (editar datos) -> despachar.
 * Los métodos que llaman a otros servicios no son @Transactional para no retener
 * una transacción de base de datos durante llamadas HTTP.
 */
@Service
@RequiredArgsConstructor
public class DespachoService {

    private final DespachoRepository despachoRepo;
    private final OrdenesClient ordenes;
    private final InventarioClient inventario;

    public DespachoResponse iniciar(Long ordenId) {
        if (despachoRepo.existsByOrdenId(ordenId)) {
            throw new ConflictException("La orden " + ordenId + " ya tiene un despacho registrado");
        }
        OrdenRemota orden = ordenes.obtener(ordenId);
        if (!"DISPONIBLE_PICKING".equals(orden.estado())) {
            throw new ConflictException("La orden " + ordenId
                    + " no está disponible para picking (estado actual: " + orden.estado() + ")");
        }
        ordenes.cambiarEstado(ordenId, "EN_PICKING", null);

        Despacho despacho = new Despacho();
        despacho.setOrdenId(ordenId);
        despacho.setComercioId(orden.comercioId());
        despacho.setEstado(EstadoDespacho.EN_PICKING);
        for (ItemRemoto item : orden.items()) {
            despacho.agregarItem(item.sku(), item.cantidad());
        }
        return aDto(despachoRepo.save(despacho));
    }

    @Transactional(readOnly = true)
    public List<DespachoResponse> listar(EstadoDespacho estado) {
        List<Despacho> despachos = estado == null
                ? despachoRepo.findAllByOrderByIdDesc()
                : despachoRepo.findByEstadoOrderByIdDesc(estado);
        return despachos.stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public DespachoResponse obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional(readOnly = true)
    public DespachoResponse obtenerPorOrden(Long ordenId) {
        return aDto(despachoRepo.findWithItemsByOrdenId(ordenId)
                .orElseThrow(() -> new NotFoundException("No existe despacho para la orden " + ordenId)));
    }

    @Transactional
    public DespachoResponse actualizar(Long id, DespachoUpdateRequest req) {
        Despacho despacho = buscar(id);
        if (despacho.getEstado() == EstadoDespacho.DESPACHADO) {
            throw new ConflictException("No se puede modificar un despacho ya despachado");
        }
        despacho.setTransportista(req.transportista());
        despacho.setObservaciones(req.observaciones());
        return aDto(despachoRepo.save(despacho));
    }

    /**
     * Registra el despacho: genera el número de seguimiento, descuenta el stock de forma definitiva
     * y marca la orden como DESPACHADA. Es reintentable: el tracking se guarda antes de llamar a otros servicios.
     */
    public DespachoResponse despachar(Long id, String transportista) {
        Despacho despacho = buscar(id);
        if (despacho.getEstado() == EstadoDespacho.DESPACHADO) {
            return aDto(despacho);
        }
        if (transportista != null && !transportista.isBlank()) {
            despacho.setTransportista(transportista);
        }
        if (despacho.getTrackingNumber() == null) {
            despacho.setTrackingNumber(generarTracking());
        }
        despacho = despachoRepo.save(despacho);

        inventario.confirmar(despacho.getOrdenId());
        ordenes.cambiarEstado(despacho.getOrdenId(), "DESPACHADA", despacho.getTrackingNumber());

        despacho.setEstado(EstadoDespacho.DESPACHADO);
        despacho.setDespachadoEn(Instant.now());
        return aDto(despachoRepo.save(despacho));
    }

    /** Cancela el picking: la orden vuelve a DISPONIBLE_PICKING. */
    public void eliminar(Long id) {
        Despacho despacho = buscar(id);
        if (despacho.getEstado() == EstadoDespacho.DESPACHADO) {
            throw new ConflictException("No se puede eliminar un despacho ya despachado");
        }
        ordenes.cambiarEstado(despacho.getOrdenId(), "DISPONIBLE_PICKING", null);
        despachoRepo.delete(despacho);
    }

    private String generarTracking() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "BN-" + fecha + "-" + sufijo;
    }

    private Despacho buscar(Long id) {
        return despachoRepo.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Despacho " + id + " no encontrado"));
    }

    private DespachoResponse aDto(Despacho d) {
        List<ItemDespachoResponse> items = d.getItems().stream()
                .map(i -> new ItemDespachoResponse(i.getSku(), i.getCantidad()))
                .toList();
        return new DespachoResponse(d.getId(), d.getOrdenId(), d.getComercioId(), d.getEstado(),
                d.getTrackingNumber(), d.getTransportista(), d.getObservaciones(), d.getCreadoEn(),
                d.getDespachadoEn(), items);
    }
}
