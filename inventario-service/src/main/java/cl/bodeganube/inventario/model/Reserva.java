package cl.bodeganube.inventario.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador de la orden (microservicio Órdenes). Único: una reserva por orden. */
    @Column(nullable = false, unique = true)
    private Long ordenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(nullable = false)
    private Instant creadaEn = Instant.now();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaItem> items = new ArrayList<>();

    public void agregarItem(Producto producto, int cantidad) {
        ReservaItem item = new ReservaItem();
        item.setReserva(this);
        item.setProducto(producto);
        item.setCantidad(cantidad);
        items.add(item);
    }
}
