package pos.repository;

import org.springframework.stereotype.Repository;
import pos.domain.Product;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {
  private final Map<Long, Product> data = new HashMap<>();
  private final AtomicLong seq = new AtomicLong(0);

  @PostConstruct
  void seed(){
    save(new Product(null,"Hamburguesa Cl?sica","Comida",6.5,true));
    save(new Product(null,"Papas Fritas","Comida",2.0,true));
    save(new Product(null,"Refresco 355ml","Bebida",1.5,true));
    save(new Product(null,"Alitas BBQ (6)","Comida",5.0,true));
    save(new Product(null,"Caf? Americano","Bebida",1.2,true));
  }

  public List<Product> findAll(){ return new ArrayList<>(data.values()); }
  public List<Product> findByCategory(String cat){
    return data.values().stream().filter(p -> Objects.equals(p.category(),cat)).collect(Collectors.toList());
  }
  public Product save(Product p){
    Long id = p.id()==null? seq.incrementAndGet() : p.id();
    var np = new Product(id, p.name(), p.category(), p.price(), p.available());
    data.put(id, np);
    return np;
  }
  public Optional<Product> findById(Long id){ return Optional.ofNullable(data.get(id)); }
}
