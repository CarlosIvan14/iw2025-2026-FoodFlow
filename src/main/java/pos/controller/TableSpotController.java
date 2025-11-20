package pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pos.domain.TableSpot;
import pos.domain.TableState;
import pos.service.TableSpotService;

import java.util.List;

@RestController
@RequestMapping("/api/table-spots")
@RequiredArgsConstructor
public class TableSpotController {

    private final TableSpotService service;

    @PostMapping
    public ResponseEntity<TableSpot> create(@RequestBody @Valid TableSpot t) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(t));
    }

    @GetMapping("/{id}")
    public TableSpot get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<TableSpot> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public TableSpot update(@PathVariable Long id, @RequestBody @Valid TableSpot t) {
        return service.update(id, t);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** Altera apenas o estado: /api/table-spots/{id}/state?state=OCUPADA */
    @PatchMapping("/{id}/state")
    public TableSpot changeState(@PathVariable Long id, @RequestParam TableState state) {
        return service.changeState(id, state);
    }
}
