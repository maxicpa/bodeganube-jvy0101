package cl.bodeganube.ordenes.repository;

import cl.bodeganube.ordenes.model.EstadoOrden;
import cl.bodeganube.ordenes.model.Orden;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Orden> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    Optional<Orden> findWithItemsByExternalId(String externalId);

    @EntityGraph(attributePaths = "items")
    List<Orden> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "items")
    List<Orden> findByEstadoOrderByIdDesc(EstadoOrden estado);

    @EntityGraph(attributePaths = "items")
    List<Orden> findByEstadoOrderByIdAsc(EstadoOrden estado);

    @EntityGraph(attributePaths = "items")
    List<Orden> findByComercioIdOrderByIdDesc(Long comercioId);

    @EntityGraph(attributePaths = "items")
    List<Orden> findByComercioIdAndEstadoOrderByIdDesc(Long comercioId, EstadoOrden estado);
}
