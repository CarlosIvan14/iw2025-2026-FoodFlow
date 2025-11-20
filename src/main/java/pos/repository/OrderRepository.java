  package pos.repository;

  import org.springframework.data.jpa.repository.JpaRepository;
  import pos.domain.Order;
  import pos.domain.OrderStatus;

  import java.util.List;

  public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);
  }
