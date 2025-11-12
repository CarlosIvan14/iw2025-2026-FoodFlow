package pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.domain.OrderDetail;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);
}



