package cl.bodeganube.picking.repository;

import cl.bodeganube.picking.model.Despacho;
import cl.bodeganube.picking.model.EstadoDespacho;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    boolean existsByOrdenId(Long ordenId);

    @EntityGraph(attributePaths = "items")
    Optional<Despacho> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    Optional<Despacho> findWithItemsByOrdenId(Long ordenId);

    @EntityGraph(attributePaths = "items")
    List<Despacho> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "items")
    List<Despacho> findByEstadoOrderByIdDesc(EstadoDespacho estado);
}
