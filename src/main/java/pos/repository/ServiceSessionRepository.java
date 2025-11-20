package pos.repository;

import pos.domain.ServiceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import pos.domain.SessionState;

import java.util.List;
import java.util.Optional;

public interface ServiceSessionRepository extends JpaRepository<ServiceSession, Long> {
    boolean existsByTableSpotIdAndState(Long tableSpotId, SessionState state);
    Optional<ServiceSession> findByTableSpotIdAndState(Long tableSpotId, SessionState state);
    List<ServiceSession> findByState(SessionState state);
}



