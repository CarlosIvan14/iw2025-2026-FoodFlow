package pos.repository;

import org.springframework.stereotype.Repository;
import pos.domain.Order;
import pos.domain.OrderItem;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {
  private final Map<Long, Order> data = new HashMap<>();
  private final AtomicLong seq = new AtomicLong(100);

  public Order save(Order o){
    Long id = (o.getId()==null)? seq.incrementAndGet() : o.getId();
    Order copy = new Order(id, o.getTableId(), o.getCustomerUsername(), o.isDelivery(),
            o.getDeliveryAddress(), o.getDeliveryPhone(), o.getItems(), o.getStatus(), 
            o.getCreatedAt()==null? Instant.now(): o.getCreatedAt());
    data.put(id, copy);
    return copy;
  }
  public Optional<Order> findById(Long id){ return Optional.ofNullable(data.get(id)); }
  public List<Order> findAll(){ return new ArrayList<>(data.values()); }
  public List<Order> findByTable(Long tableId){
    var out = new ArrayList<Order>();
    for(var o: data.values()) if(Objects.equals(o.getTableId(), tableId)) out.add(o);
    return out;
  }
  public List<Order> findKitchenQueue(){
    var list = new ArrayList<>(data.values());
    list.sort(Comparator.<Order, Long>comparing(o -> o.getTableId()==null?0:o.getTableId())
      .thenComparing(Order::getCreatedAt));
    return list;
  }
}
