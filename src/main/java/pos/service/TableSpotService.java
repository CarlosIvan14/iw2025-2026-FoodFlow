package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pos.domain.TableSpot;
import pos.domain.TableState;
import pos.repository.TableSpotRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TableSpotService {

    private final TableSpotRepository tableSpotRepository;

    public TableSpot create(TableSpot t) {
        try {
            if (t.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New table must not have an id");
            }
            validate(t);
            TableSpot saved = tableSpotRepository.save(t);
            log.info("TableSpot created id={} code={}", saved.getId(), saved.getCode());
            return saved;

        } catch (ResponseStatusException ex) {
            log.warn("Create table failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error creating table code={}", t != null ? t.getCode() : null, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while creating table");
        } catch (RuntimeException ex) {
            log.error("Unexpected error creating table", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while creating table");
        }
    }

    @Transactional(readOnly = true)
    public TableSpot get(Long id) {
        try {
            return tableSpotRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found id=" + id));
        } catch (DataAccessException ex) {
            log.error("DB error reading table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while fetching table");
        } catch (RuntimeException ex) {
            log.error("Unexpected error reading table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while fetching table");
        }
    }

    @Transactional(readOnly = true)
    public List<TableSpot> list() {
        try {
            return tableSpotRepository.findAll();
        } catch (DataAccessException ex) {
            log.error("DB error listing tables", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while listing tables");
        } catch (RuntimeException ex) {
            log.error("Unexpected error listing tables", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while listing tables");
        }
    }

    public TableSpot update(Long id, TableSpot payload) {
        try {
            TableSpot t = tableSpotRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found id=" + id));

            t.setCode(payload.getCode());
            t.setCapacity(payload.getCapacity());
            t.setState(payload.getState());
            t.setNote(payload.getNote());

            validate(t);

            log.info("TableSpot updated id={} code={}", id, t.getCode());
            return t;

        } catch (ResponseStatusException ex) {
            log.warn("Update table failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error updating table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while updating table");
        } catch (RuntimeException ex) {
            log.error("Unexpected error updating table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while updating table");
        }
    }

    public void delete(Long id) {
        try {
            TableSpot t = tableSpotRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found id=" + id));
            tableSpotRepository.delete(t);
            log.info("TableSpot deleted id={}", id);

        } catch (ResponseStatusException ex) {
            log.warn("Delete table failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error deleting table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while deleting table");
        } catch (RuntimeException ex) {
            log.error("Unexpected error deleting table id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while deleting table");
        }
    }

    /** Mudança rápida de estado: LIBRE / OCUPADA / RESERVADA */
    public TableSpot changeState(Long id, TableState state) {
        try {
            if (state == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "state is required");
            }
            TableSpot t = tableSpotRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found id=" + id));
            t.setState(state.getState());
            log.info("TableSpot state changed id={} -> {}", id, state);
            return t;

        } catch (ResponseStatusException ex) {
            log.warn("Change table state failed: {}", ex.getReason());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("DB error changing table state id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error while changing table state");
        } catch (RuntimeException ex) {
            log.error("Unexpected error changing table state id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while changing table state");
        }
    }

    private void validate(TableSpot t) {
        if (t.getCode() == null || t.getCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table code is required");
        }
        if (t.getCapacity() == null || t.getCapacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be > 0");
        }
        if (t.getState() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State is required");
        }
    }
}

