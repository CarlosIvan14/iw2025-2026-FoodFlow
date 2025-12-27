package pos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.Ingredient;
import pos.repository.IngredientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository repository;

    public List<Ingredient> list() {
        return repository.findAll();
    }

    public Ingredient get(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));
    }

    @Transactional
    public Ingredient save(Ingredient ingredient) {
        return repository.save(ingredient);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}