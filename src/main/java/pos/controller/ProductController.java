package pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pos.domain.Product;
import pos.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid Product p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p));
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Product> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody @Valid Product p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** Ajusta estoque por delta (ex.: ?delta=-1 para saída, +5 para entrada). */
    @PatchMapping("/{id}/stock")
    public Product adjustStock(@PathVariable Long id, @RequestParam Integer delta) {
        return service.adjustStock(id, delta);
    }
}
