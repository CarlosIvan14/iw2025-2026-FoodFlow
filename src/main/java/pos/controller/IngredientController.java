package pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.domain.Ingredient;
import pos.service.IngredientService;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @GetMapping
    public List<Ingredient> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> get(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.get(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Ingredient> create(@RequestBody @Valid Ingredient ingredient) {
        // Garantimos que é um novo registro zerando o ID
        ingredient.setId(null);
        Ingredient saved = service.save(ingredient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> update(@PathVariable Long id, @RequestBody @Valid Ingredient ingredient) {
        // Força o ID da URL no objeto para garantir a atualização correta
        ingredient.setId(id);
        try {
            Ingredient updated = service.save(ingredient);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}