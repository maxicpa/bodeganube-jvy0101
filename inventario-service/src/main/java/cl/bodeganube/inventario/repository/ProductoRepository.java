package cl.bodeganube.inventario.repository;

import cl.bodeganube.inventario.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySku(String sku);

    /** Bloqueo pesimista ordenado por SKU (evita deadlocks entre reservas concurrentes). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.sku in :skus order by p.sku")
    List<Producto> findBySkuInParaActualizar(@Param("skus") Collection<String> skus);
}
