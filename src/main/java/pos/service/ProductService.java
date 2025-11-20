package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pos.domain.Product;
import pos.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(Product p) {
        try {
            if (p.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New product must not have an id");
            }
            validate(p);
            Product saved = productRepository.save(p);
            log.info("Product created id={}", saved.getId());
            return saved;

        } catch (ResponseStatusException ex) {
            log.warn("Create product failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error creating product name={}", p != null ? p.getName() : null, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while creating product");
        } catch (RuntimeException ex) {
            log.error("Unexpected error creating product", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while creating product");
        }
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        try {
            return productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found id=" + id));
        } catch (DataAccessException ex) {
            log.error("DB error reading product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while fetching product");
        } catch (RuntimeException ex) {
            log.error("Unexpected error reading product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while fetching product");
        }
    }

    @Transactional(readOnly = true)
    public List<Product> list() {
        try {
            return productRepository.findAll();
        } catch (DataAccessException ex) {
            log.error("DB error listing products", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while listing products");
        } catch (RuntimeException ex) {
            log.error("Unexpected error listing products", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while listing products");
        }
    }

    public Product update(Long id, Product payload) {
        try {
            Product p = productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found id=" + id));

            // aplica alterações
            p.setName(payload.getName());
            p.setDescription(payload.getDescription());
            p.setPrice(payload.getPrice());
            p.setStock(payload.getStock());

            validate(p); // garante price>=0, stock>=0

            log.info("Product updated id={}", id);
            return p;

        } catch (ResponseStatusException ex) {
            log.warn("Update product failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error updating product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while updating product");
        } catch (RuntimeException ex) {
            log.error("Unexpected error updating product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while updating product");
        }
    }

    public void delete(Long id) {
        try {
            Product p = productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found id=" + id));
            productRepository.delete(p);
            log.info("Product deleted id={}", id);

        } catch (ResponseStatusException ex) {
            log.warn("Delete product failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error deleting product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while deleting product");
        } catch (RuntimeException ex) {
            log.error("Unexpected error deleting product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while deleting product");
        }
    }

    /** Ajuste de estoque por delta (pode ser negativo). */
    public Product adjustStock(Long id, Integer delta) {
        try {
            if (delta == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "delta is required");
            }
            Product p = productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found id=" + id));

            int newStock = (p.getStock() == null ? 0 : p.getStock()) + delta;
            if (newStock < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resulting stock cannot be negative");
            }
            p.setStock(newStock);
            log.info("Product stock adjusted id={}, delta={}, newStock={}", id, delta, newStock);
            return p;

        } catch (ResponseStatusException ex) {
            log.warn("Adjust stock failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error adjusting stock for product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while adjusting stock");
        } catch (RuntimeException ex) {
            log.error("Unexpected error adjusting stock for product id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while adjusting stock");
        }
    }

    private void validate(Product p) {
        if (p.getName() == null || p.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be >= 0");
        }
        if (p.getStock() != null && p.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock must be >= 0");
        }
    }
}
