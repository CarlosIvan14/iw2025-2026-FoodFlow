package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.Ingredient;
import pos.repository.IngredientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientService {

    private final IngredientRepository repository;

    public List<Ingredient> list() {
        log.info("Listando todos los ingredientes");
        return repository.findAll();
    }

    public Ingredient get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de acceso a ingrediente inexistente ID: {}", id);
                    return new RuntimeException("Ingrediente no encontrado con ID: " + id);
                });
    }

    @Transactional
    public Ingredient save(Ingredient ingredient) {
        try {
            log.info("Guardando ingrediente: {}", ingredient.getNombre());
            return repository.save(ingredient);

        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al guardar ingrediente: {}", e.getMessage());
            throw new RuntimeException("Error: Ya existe un ingrediente con este nombre o los datos son inválidos.");

        } catch (Exception e) {
            log.error("Error inesperado al guardar ingrediente", e);
            throw new RuntimeException("Error interno al guardar el ingrediente. Intente nuevamente.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: El ingrediente no existe.");
        }

        try {
            log.info("Eliminando ingrediente ID: {}", id);
            repository.deleteById(id);

        } catch (Exception e) {
            log.error("Error al eliminar ingrediente ID {}", id, e);
            throw new RuntimeException("Error al eliminar el ingrediente.");
        }
    }
}