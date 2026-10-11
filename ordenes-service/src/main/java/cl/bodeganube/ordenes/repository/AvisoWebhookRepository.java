package cl.bodeganube.ordenes.repository;

import cl.bodeganube.ordenes.model.AvisoWebhook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoWebhookRepository extends JpaRepository<AvisoWebhook, Long> {

    boolean existsByExternalId(String externalId);

    List<AvisoWebhook> findAllByOrderByIdDesc();
}
