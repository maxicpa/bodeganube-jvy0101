package cl.bodeganube.auth.repository;

import cl.bodeganube.auth.model.Comercio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComercioRepository extends JpaRepository<Comercio, Long> {

    boolean existsByRut(String rut);

    boolean existsByNombre(String nombre);
}
