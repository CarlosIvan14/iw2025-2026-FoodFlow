package pos.repository;

import pos.domain.Product;
import pos.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    public List<Product> findAll();
    public List<Product> findByCategory(Category category);

}

