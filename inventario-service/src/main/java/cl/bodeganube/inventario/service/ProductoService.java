package cl.bodeganube.inventario.service;

import cl.bodeganube.inventario.dto.AjusteStockRequest;
import cl.bodeganube.inventario.dto.ProductoCreateRequest;
import cl.bodeganube.inventario.dto.ProductoResponse;
import cl.bodeganube.inventario.dto.ProductoUpdateRequest;
import cl.bodeganube.inventario.exception.ConflictException;
import cl.bodeganube.inventario.exception.NotFoundException;
import cl.bodeganube.inventario.model.Producto;
import cl.bodeganube.inventario.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repo;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return repo.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoCreateRequest r) {
        if (repo.existsBySku(r.sku())) {
            throw new ConflictException("Ya existe un producto con el SKU " + r.sku());
        }
        Producto p = new Producto();
        p.setSku(r.sku());
        p.setNombre(r.nombre());
        p.setDescripcion(r.descripcion());
        p.setStockDisponible(r.stockInicial());
        return aDto(repo.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoUpdateRequest r) {
        Producto p = buscar(id);
        p.setNombre(r.nombre());
        p.setDescripcion(r.descripcion());
        return aDto(repo.save(p));
    }

    @Transactional
    public ProductoResponse ajustarStock(Long id, AjusteStockRequest r) {
        Producto p = buscar(id);
        int nuevo = p.getStockDisponible() + r.cantidad();
        if (nuevo < 0) {
            throw new ConflictException("El ajuste dejaría el stock disponible en negativo (actual: "
                    + p.getStockDisponible() + ", ajuste: " + r.cantidad() + ")");
        }
        p.setStockDisponible(nuevo);
        return aDto(repo.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        if (p.getStockReservado() > 0) {
            throw new ConflictException("No se puede eliminar: el producto tiene stock reservado");
        }
        repo.delete(p);
    }

    private Producto buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Producto " + id + " no encontrado"));
    }

    private ProductoResponse aDto(Producto p) {
        return new ProductoResponse(p.getId(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getStockDisponible(), p.getStockReservado());
    }
}
