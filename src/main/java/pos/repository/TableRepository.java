package pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.domain.TableSpot;

public interface TableRepository extends JpaRepository<TableSpot, Long> {
}
