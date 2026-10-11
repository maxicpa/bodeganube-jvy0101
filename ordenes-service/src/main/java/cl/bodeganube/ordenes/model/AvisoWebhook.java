package cl.bodeganube.ordenes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro de cada aviso de venta recibido; su externalId único evita procesar duplicados. */
@Entity
@Table(name = "avisos_webhook")
@Getter
@Setter
@NoArgsConstructor
public class AvisoWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String externalId;

    @Column(nullable = false)
    private Long comercioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAviso estado;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false)
    private Instant recibidoEn = Instant.now();
}
