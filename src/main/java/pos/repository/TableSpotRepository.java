package pos.repository;

import pos.domain.TableSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TableSpotRepository extends JpaRepository<TableSpot, Long> {
    Optional<TableSpot> findByCode(String code);
}

