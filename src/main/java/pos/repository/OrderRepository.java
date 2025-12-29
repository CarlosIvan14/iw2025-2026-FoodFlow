// src/main/java/pos/repository/OrderRepository.java
package pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pos.domain.Order;
import pos.domain.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select distinct o
        from Order o
        left join fetch o.items it
        left join fetch it.product p
        where o.status in (pos.domain.OrderStatus.IN_PREPARATION, pos.domain.OrderStatus.LISTO)
        order by o.createdAt asc
    """)
    List<Order> findKitchenQueue();

    List<Order> findByStatus(OrderStatus status);

    /**
     * ✅ Para Analytics: pedidos + items + product ya inicializados
     */
    @Query("""
        select distinct o
        from Order o
        left join fetch o.items it
        left join fetch it.product p
        where o.status in :statuses
          and o.createdAt between :start and :end
        order by o.createdAt asc
    """)
    List<Order> findWithItemsBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            @Param("statuses") List<OrderStatus> statuses
    );

    /**
     * ✅ Activos por mesa (usando serviceSession)
     * OJO: ajusta "s.tableSpot.id" si tu relación se llama diferente.
     */
    @Query("""
        select distinct o
        from Order o
        join o.serviceSession s
        join s.tableSpot t
        where t.id = :tableId
          and o.status not in :statuses
        order by o.createdAt desc
    """)
    List<Order> findActiveByTableSpotIdAndStatusNotIn(
            @Param("tableId") Long tableId,
            @Param("statuses") List<OrderStatus> statuses
    );
}
