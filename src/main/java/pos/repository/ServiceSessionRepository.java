package pos.repository;

import pos.domain.ServiceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import pos.domain.SessionState;

import java.util.List;

public interface ServiceSessionRepository extends JpaRepository<ServiceSession, Long> {
    List<ServiceSession> findByWaiterId(Long waiterId);
    List<ServiceSession> findByState(SessionState state);
}

