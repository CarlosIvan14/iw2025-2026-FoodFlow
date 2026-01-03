package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pos.domain.Category;
import pos.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) {
        try {
            if (category.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría nueva no debe tener id");
            }
            
            if (categoryRepository.findByNombre(category.getNombre()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría ya existe: " + category.getNombre());
            }
            
            Category saved = categoryRepository.save(category);
            log.info("Categoría creada id={} nombre={}", saved.getId(), saved.getNombre());
            return saved;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Error DB creando categoría nombre={}", category.getNombre(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en base de datos al crear categoría");
        }
    }

    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Category> list() {
        try {
            return categoryRepository.findAll();
        } catch (DataAccessException ex) {
            log.error("Error DB listando categorías", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en base de datos al listar categorías");
        }
    }

    public Category update(Long id, Category payload) {
        try {
            Category category = get(id);
            
            if (!category.getNombre().equalsIgnoreCase(payload.getNombre()) &&
                    categoryRepository.findByNombre(payload.getNombre()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría ya existe: " + payload.getNombre());
            }
            
            category.setNombre(payload.getNombre());
            category.setDescripcion(payload.getDescripcion());
            
            log.info("Categoría actualizada id={}", id);
            return category;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Error DB actualizando categoría id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en base de datos al actualizar categoría");
        }
    }

    public void delete(Long id) {
        try {
            Category category = get(id);
            categoryRepository.delete(category);
            log.info("Categoría eliminada id={}", id);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Error DB eliminando categoría id={}", id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en base de datos al eliminar categoría");
        }
    }
}
