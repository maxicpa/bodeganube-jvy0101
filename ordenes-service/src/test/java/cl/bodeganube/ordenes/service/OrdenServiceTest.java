package cl.bodeganube.ordenes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bodeganube.ordenes.cliente.InventarioClient;
import cl.bodeganube.ordenes.cliente.InventarioClient.ResultadoReserva;
import cl.bodeganube.ordenes.dto.EstadoInternoRequest;
import cl.bodeganube.ordenes.dto.ItemVentaRequest;
import cl.bodeganube.ordenes.dto.OrdenResponse;
import cl.bodeganube.ordenes.dto.VentaRequest;
import cl.bodeganube.ordenes.exception.ConflictException;
import cl.bodeganube.ordenes.model.EstadoOrden;
import cl.bodeganube.ordenes.model.Orden;
import cl.bodeganube.ordenes.repository.OrdenRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrdenServiceTest {

    @Mock
    OrdenRepository ordenRepo;
    @Mock
    InventarioClient inventario;
    @InjectMocks
    OrdenService service;

    VentaRequest venta;

    @BeforeEach
    void preparar() {
        venta = new VentaRequest("SHOP-1001", 1L, "Cliente Prueba", List.of(new ItemVentaRequest("SKU-001", 2)));
        when(ordenRepo.findWithItemsByExternalId("SHOP-1001")).thenReturn(Optional.empty());
        when(ordenRepo.save(any(Orden.class))).thenAnswer(inv -> {
            Orden o = inv.getArgument(0);
            if (o.getId() == null) {
                o.setId(1L);
            }
            return o;
        });
    }

    @Test
    void crear_conStock_quedaDisponibleParaPicking() {
        when(inventario.reservar(eq(1L), anyList())).thenReturn(new ResultadoReserva(true, null));

        OrdenResponse r = service.crear(venta);

        assertEquals(EstadoOrden.DISPONIBLE_PICKING, r.estado());
    }

    @Test
    void crear_sinStock_quedaSinStockConMotivo() {
        when(inventario.reservar(eq(1L), anyList()))
                .thenReturn(new ResultadoReserva(false, "Stock insuficiente: SKU-001"));

        OrdenResponse r = service.crear(venta);

        assertEquals(EstadoOrden.SIN_STOCK, r.estado());
        assertEquals("Stock insuficiente: SKU-001", r.motivo());
    }

    @Test
    void crear_externalIdRepetido_noVuelveAReservar() {
        Orden existente = new Orden();
        existente.setId(7L);
        existente.setExternalId("SHOP-1001");
        existente.setEstado(EstadoOrden.DISPONIBLE_PICKING);
        when(ordenRepo.findWithItemsByExternalId("SHOP-1001")).thenReturn(Optional.of(existente));

        OrdenResponse r = service.crear(venta);

        assertEquals(7L, r.id());
        verify(inventario, never()).reservar(any(), anyList());
    }

    @Test
    void cambiarEstadoInterno_transicionInvalida_lanzaConflict() {
        Orden orden = new Orden();
        orden.setId(3L);
        orden.setEstado(EstadoOrden.SIN_STOCK);
        when(ordenRepo.findWithItemsById(3L)).thenReturn(Optional.of(orden));

        assertThrows(ConflictException.class,
                () -> service.cambiarEstadoInterno(3L, new EstadoInternoRequest(EstadoOrden.EN_PICKING, null)));
    }
}
