package cl.bodeganube.inventario.config;

import cl.bodeganube.inventario.model.Producto;
import cl.bodeganube.inventario.repository.ProductoRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Catálogo de demostración (solo si la tabla está vacía). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner semillaProductos(ProductoRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.saveAll(List.of(
                    producto("SKU-001", "Polera básica", 50),
                    producto("SKU-002", "Jeans clásico", 30),
                    producto("SKU-003", "Zapatillas urbanas", 10),
                    producto("SKU-004", "Mochila 20L", 5)));
        };
    }

    private static Producto producto(String sku, String nombre, int stock) {
        Producto p = new Producto();
        p.setSku(sku);
        p.setNombre(nombre);
        p.setStockDisponible(stock);
        return p;
    }
}
