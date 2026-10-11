package cl.bodeganube.picking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bodeganube.picking.cliente.InventarioClient;
import cl.bodeganube.picking.cliente.OrdenesClient;
import cl.bodeganube.picking.cliente.OrdenesClient.OrdenRemota;
import cl.bodeganube.picking.dto.DespachoResponse;
import cl.bodeganube.picking.exception.ConflictException;
import cl.bodeganube.picking.model.Despacho;
import cl.bodeganube.picking.model.EstadoDespacho;
import cl.bodeganube.picking.repository.DespachoRepository;
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
class DespachoServiceTest {

    @Mock
    DespachoRepository despachoRepo;
    @Mock
    OrdenesClient ordenes;
    @Mock
    InventarioClient inventario;
    @InjectMocks
    DespachoService service;

    @BeforeEach
    void preparar() {
        when(despachoRepo.save(any(Despacho.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void iniciar_ordenNoDisponible_lanzaConflictYNoCambiaEstado() {
        when(despachoRepo.existsByOrdenId(5L)).thenReturn(false);
        when(ordenes.obtener(5L)).thenReturn(new OrdenRemota(5L, 1L, "SIN_STOCK", List.of()));

        assertThrows(ConflictException.class, () -> service.iniciar(5L));
        verify(ordenes, never()).cambiarEstado(any(), anyString(), any());
    }

    @Test
    void iniciar_ordenDisponible_creaDespachoEnPicking() {
        when(despachoRepo.existsByOrdenId(5L)).thenReturn(false);
        when(ordenes.obtener(5L)).thenReturn(new OrdenRemota(5L, 1L, "DISPONIBLE_PICKING",
                List.of(new OrdenesClient.ItemRemoto("SKU-001", 2))));

        DespachoResponse r = service.iniciar(5L);

        assertEquals(EstadoDespacho.EN_PICKING, r.estado());
        assertEquals(1, r.items().size());
        verify(ordenes).cambiarEstado(5L, "EN_PICKING", null);
    }

    @Test
    void despachar_generaTrackingConfirmaStockYMarcaOrdenDespachada() {
        Despacho d = new Despacho();
        d.setId(1L);
        d.setOrdenId(5L);
        d.setComercioId(1L);
        d.setEstado(EstadoDespacho.EN_PICKING);
        d.agregarItem("SKU-001", 2);
        when(despachoRepo.findWithItemsById(1L)).thenReturn(Optional.of(d));

        DespachoResponse r = service.despachar(1L, "Chilexpress");

        assertEquals(EstadoDespacho.DESPACHADO, r.estado());
        assertTrue(r.trackingNumber().startsWith("BN-"));
        verify(inventario).confirmar(5L);
        verify(ordenes).cambiarEstado(eq(5L), eq("DESPACHADA"), anyString());
    }
}
