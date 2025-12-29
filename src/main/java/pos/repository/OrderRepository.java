package pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import pos.domain.Order;
import pos.domain.OrderStatus;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

  // ✅ Carga items, serviceSession y sus relaciones de una vez
  @EntityGraph(attributePaths = {"items", "serviceSession", "serviceSession.tableSpot"})
  @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'IN_PREPARATION') ORDER BY o.createdAt ASC")
  List<Order> findKitchenQueue();

  // Para otras consultas que necesiten serviceSession
  @EntityGraph(attributePaths = {"serviceSession", "serviceSession.tableSpot"})
  @Query("SELECT o FROM Order o WHERE o.status = ?1 ORDER BY o.createdAt DESC")
  List<Order> findByStatus(OrderStatus status);

  // Para findActiveOrdersByTable
  @EntityGraph(attributePaths = {"serviceSession", "serviceSession.tableSpot"})
  @Query("SELECT o FROM Order o WHERE o.serviceSession.tableSpot.id = ?1 AND o.status NOT IN ?2 ORDER BY o.createdAt DESC")
  List<Order> findByTableIdAndStatusNotIn(Long tableId, java.util.List<OrderStatus> statuses);

  List<Order> findAll();
}
