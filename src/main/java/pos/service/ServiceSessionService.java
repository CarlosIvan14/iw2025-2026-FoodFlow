package pos.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.*;
import pos.repository.ServiceSessionRepository;
import pos.repository.TableSpotRepository;
import pos.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceSessionService {

    private final ServiceSessionRepository serviceSessionRepository;
    private final TableSpotRepository tableSpotRepository;
    private final UserRepository userRepository;

    /**
     * Abre uma sessão de serviço para uma mesa (Dine-in).
     */
    public ServiceSession openSession(Long tableSpotId, Long waiterId) {
        try {
            TableSpot table = tableSpotRepository.findById(tableSpotId)
                    .orElseThrow(() -> new EntityNotFoundException("TableSpot not found id=" + tableSpotId));

            // Garante que não exista outra sessão ABERTA para essa mesa
            if (serviceSessionRepository.existsByTableSpotIdAndState(
                    tableSpotId, SessionState.OPEN)) {
                throw new IllegalStateException("Table " + table.getCode() + " already has an OPEN session");
            }

            User waiter = userRepository.findById(waiterId)
                    .orElseThrow(() -> new EntityNotFoundException("User (waiter) not found id=" + waiterId));

            // Se quiser, dá pra validar o papel do usuário aqui (Role.WAITER)
            // if (waiter.getRole() != User.Role.WAITER) { ... }

            ServiceSession session = ServiceSession.builder()
                    .tableSpot(table)
                    .waiter(waiter)
                    .openedAt(OffsetDateTime.now())
                    .state(SessionState.OPEN)
                    .build();

            // Atualiza estado da mesa para OCCUPIED
            table.setState(TableState.OCCUPIED);
            tableSpotRepository.save(table);

            ServiceSession saved = serviceSessionRepository.save(session);
            log.info("Opened ServiceSession id={} table={} waiter={}",
                    saved.getId(), table.getCode(), waiter.getEmail());

            return saved;
        } catch (RuntimeException ex) {
            log.warn("openSession failed tableSpotId={} waiterId={}: {}",
                    tableSpotId, waiterId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Fecha uma sessão de serviço, liberando a mesa.
     * (Futuramente aqui podemos validar se todos os pedidos estão pagos.)
     */
    public ServiceSession closeSession(Long sessionId) {
        try {
            ServiceSession session = serviceSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("ServiceSession not found id=" + sessionId));

            if (session.getState() == SessionState.CLOSED) {
                log.info("ServiceSession id={} already CLOSED", sessionId);
                return session;
            }

            session.setState(SessionState.CLOSED);
            session.setClosedAt(OffsetDateTime.now());

            TableSpot table = session.getTableSpot();
            if (table != null) {
                table.setState(TableState.FREE);
                tableSpotRepository.save(table);
            }

            log.info("Closed ServiceSession id={} for table={}",
                    session.getId(), table != null ? table.getCode() : "N/A");

            // session é entidade gerenciada dentro da @Transactional, não precisa de save explícito
            return session;
        } catch (RuntimeException ex) {
            log.warn("closeSession failed sessionId={}: {}", sessionId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Busca uma sessão por id.
     */
    @Transactional(readOnly = true)
    public ServiceSession get(Long id) {
        return serviceSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ServiceSession not found id=" + id));
    }

    /**
     * Lista todas as sessões com status OPEN (para dashboard de mesas).
     */
    @Transactional(readOnly = true)
    public List<ServiceSession> findOpenSessions() {
        return serviceSessionRepository.findByState(SessionState.OPEN);
    }

    /**
     * Busca sessão aberta de uma mesa específica (se existir).
     */
    @Transactional(readOnly = true)
    public ServiceSession findOpenByTable(Long tableSpotId) {
        return serviceSessionRepository.findByTableSpotIdAndState(
                        tableSpotId, SessionState.OPEN)
                .orElseThrow(() ->
                        new EntityNotFoundException("No OPEN ServiceSession for tableSpot id=" + tableSpotId));
    }
}
