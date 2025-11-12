package pos.repository;

import pos.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import pos.domain.PaymentStatus;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySaleId(Long saleId);
    List<Payment> findByStatus(PaymentStatus status);
}

