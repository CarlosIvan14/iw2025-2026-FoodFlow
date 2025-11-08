package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.Order;
import pos.domain.OrderItem;
import pos.repository.OrderRepository;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
  private final OrderRepository repo;
  public OrderService(OrderRepository repo){ this.repo = repo; }

  public Order createTableOrder(Long tableId, List<OrderItem> items, String createdBy){
    return repo.save(new Order(null, tableId, createdBy, false, null, null, items, Order.Status.PENDIENTE, Instant.now()));
  }
  public Order createCustomerOrder(boolean delivery, String address, String phone, List<OrderItem> items, String username){
    return repo.save(new Order(null, null, username, delivery, address, phone, items, Order.Status.PENDIENTE, Instant.now()));
  }
  public List<Order> all(){ return repo.findAll(); }
  public List<Order> kitchenQueue(){ return repo.findKitchenQueue(); }
  public void updateStatus(Long id, Order.Status s){
    repo.findById(id).ifPresent(o -> { o.setStatus(s); repo.save(o); });
  }
}
