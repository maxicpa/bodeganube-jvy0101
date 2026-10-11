package cl.bodeganube.ordenes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del pedido en el canal externo (Shopify, etc.). Único: base de la idempotencia. */
    @Column(nullable = false, unique = true, length = 100)
    private String externalId;

    /** Comercio dueño de la orden (referencia lógica a auth-service). */
    @Column(nullable = false)
    private Long comercioId;

    @Column(nullable = false, length = 150)
    private String cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado;

    @Column(length = 500)
    private String motivo;

    @Column(length = 50)
    private String trackingNumber;

    @Column(nullable = false)
    private Instant creadaEn = Instant.now();

    @Column(nullable = false)
    private Instant actualizadaEn = Instant.now();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenItem> items = new ArrayList<>();

    public void agregarItem(String sku, int cantidad) {
        OrdenItem item = new OrdenItem();
        item.setOrden(this);
        item.setSku(sku);
        item.setCantidad(cantidad);
        items.add(item);
    }

    @PreUpdate
    void alActualizar() {
        this.actualizadaEn = Instant.now();
    }
}
