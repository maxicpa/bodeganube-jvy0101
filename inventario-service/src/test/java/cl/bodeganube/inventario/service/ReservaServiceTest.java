package cl.bodeganube.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.bodeganube.inventario.dto.ItemReservaRequest;
import cl.bodeganube.inventario.dto.ReservaRequest;
import cl.bodeganube.inventario.dto.ReservaResponse;
import cl.bodeganube.inventario.exception.ConflictException;
import cl.bodeganube.inventario.exception.NotFoundException;
import cl.bodeganube.inventario.model.EstadoReserva;
import cl.bodeganube.inventario.model.Producto;
import cl.bodeganube.inventario.model.Reserva;
import cl.bodeganube.inventario.repository.ProductoRepository;
import cl.bodeganube.inventario.repository.ReservaRepository;
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
class ReservaServiceTest {

    @Mock
    ProductoRepository productoRepo;
    @Mock
    ReservaRepository reservaRepo;
    @InjectMocks
    ReservaService service;

    Producto producto;

    @BeforeEach
    void preparar() {
        producto = new Producto();
        producto.setId(1L);
        producto.setSku("SKU-1");
        producto.setNombre("Producto de prueba");
        producto.setStockDisponible(10);
        producto.setStockReservado(0);

        when(reservaRepo.findByOrdenId(1L)).thenReturn(Optional.empty());
        when(productoRepo.findBySkuInParaActualizar(any())).thenReturn(List.of(producto));
        when(reservaRepo.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void reservar_conStockSuficiente_descuentaDisponibleYSumaReservado() {
        ReservaResponse r = service.reservar(new ReservaRequest(1L, List.of(new ItemReservaRequest("SKU-1", 3))));

        assertEquals(EstadoReserva.ACTIVA, r.estado());
        assertEquals(7, producto.getStockDisponible());
        assertEquals(3, producto.getStockReservado());
    }

    @Test
    void reservar_conStockInsuficiente_lanzaConflictYNoModificaStock() {
        var request = new ReservaRequest(1L, List.of(new ItemReservaRequest("SKU-1", 20)));

        assertThrows(ConflictException.class, () -> service.reservar(request));
        assertEquals(10, producto.getStockDisponible());
        assertEquals(0, producto.getStockReservado());
    }

    @Test
    void reservar_conSkuInexistente_lanzaNotFound() {
        var request = new ReservaRequest(1L, List.of(new ItemReservaRequest("SKU-NO-EXISTE", 1)));

        assertThrows(NotFoundException.class, () -> service.reservar(request));
    }

    @Test
    void reservar_ordenYaReservada_esIdempotente() {
        Reserva existente = new Reserva();
        existente.setOrdenId(1L);
        existente.setEstado(EstadoReserva.ACTIVA);
        when(reservaRepo.findByOrdenId(1L)).thenReturn(Optional.of(existente));

        ReservaResponse r = service.reservar(new ReservaRequest(1L, List.of(new ItemReservaRequest("SKU-1", 3))));

        assertEquals(EstadoReserva.ACTIVA, r.estado());
        verify(productoRepo, never()).findBySkuInParaActualizar(any());
        assertEquals(10, producto.getStockDisponible());
    }
}
