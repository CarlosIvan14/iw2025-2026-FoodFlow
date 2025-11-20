package pos.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pos.domain.ServiceSession;
import pos.service.ServiceSessionService;

import java.util.List;

@RestController
@RequestMapping("/api/service-sessions")
@RequiredArgsConstructor
@Slf4j
public class ServiceSessionController {

    private final ServiceSessionService serviceSessionService;

    /**
     * Abre uma sessão de mesa (Dine-in).
     * Exemplo:
     *  POST /api/service-sessions/open?tableSpotId=1&waiterId=2
     */
    @PostMapping("/open")
    public ServiceSession openSession(@RequestParam Long tableSpotId,
                                      @RequestParam Long waiterId) {
        log.info("API openSession tableSpotId={} waiterId={}", tableSpotId, waiterId);
        return serviceSessionService.openSession(tableSpotId, waiterId);
    }

    /**
     * Fecha uma sessão específica.
     * Exemplo:
     *  POST /api/service-sessions/5/close
     */
    @PostMapping("/{id}/close")
    public ServiceSession closeSession(@PathVariable Long id) {
        log.info("API closeSession id={}", id);
        return serviceSessionService.closeSession(id);
    }

    /**
     * Busca a sessão aberta de uma mesa (se existir).
     * Exemplo:
     *  GET /api/service-sessions/by-table/1
     */
    @GetMapping("/open")
    public List<ServiceSession> listOpen() {
        log.info("API listOpen sessions");
        return serviceSessionService.findOpenSessions();
    }

    @GetMapping("/by-table/{tableSpotId}")
    public ServiceSession getActiveByTable(@PathVariable Long tableSpotId) {
        log.info("API getActiveByTable tableSpotId={}", tableSpotId);
        return serviceSessionService.findOpenByTable(tableSpotId);
    }

    /**
     * Busca sessão por ID.
     * Exemplo:
     *  GET /api/service-sessions/3
     */
    @GetMapping("/{id}")
    public ServiceSession getById(@PathVariable Long id) {
        log.info("API getById id={}", id);
        return serviceSessionService.get(id);
    }
}
