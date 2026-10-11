package cl.bodeganube.picking.model;

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
@Table(name = "despachos")
@Getter
@Setter
@NoArgsConstructor
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Orden asociada (microservicio Órdenes). Un solo despacho por orden. */
    @Column(nullable = false, unique = true)
    private Long ordenId;

    @Column(nullable = false)
    private Long comercioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDespacho estado;

    @Column(unique = true, length = 50)
    private String trackingNumber;

    @Column(length = 80)
    private String transportista;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false)
    private Instant creadoEn = Instant.now();

    private Instant despachadoEn;

    @OneToMany(mappedBy = "despacho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespachoItem> items = new ArrayList<>();

    public void agregarItem(String sku, int cantidad) {
        DespachoItem item = new DespachoItem();
        item.setDespacho(this);
        item.setSku(sku);
        item.setCantidad(cantidad);
        items.add(item);
    }
}
