package cl.bodeganube.inventario.repository;

import cl.bodeganube.inventario.model.Reserva;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @EntityGraph(attributePaths = {"items", "items.producto"})
    Optional<Reserva> findByOrdenId(Long ordenId);
}
