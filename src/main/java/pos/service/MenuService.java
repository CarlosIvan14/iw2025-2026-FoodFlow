package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.Product;
import pos.repository.ProductRepository;

import java.util.List;

@Service
public class MenuService {
  private final ProductRepository products;
  public MenuService(ProductRepository products){ this.products = products; }
  public List<Product> list(){ return products.findAll(); }
  public List<Product> byCategory(String cat){ return products.findByCategory(cat); }
}
