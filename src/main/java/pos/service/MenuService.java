package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.Product;
import java.util.List;
import java.util.ArrayList;

@Service
public class MenuService {

    private final pos.repository.ProductRepository productRepository;

    public MenuService(pos.repository.ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> list() {
        return productRepository.findAll();
    }

    public List<Product> byCategory(pos.domain.Category category) {
        return productRepository.findByCategory(category);
    }
}
