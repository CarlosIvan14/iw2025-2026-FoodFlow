package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.TableSpot;
import pos.repository.TableRepository;
import java.util.List;

@Service
public class TableService {

    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<TableSpot> findAll() {
        return tableRepository.findAll();
    }

    public List<TableSpot> all() {
        return findAll();
    }

    public TableSpot save(TableSpot table) {
        return tableRepository.save(table);
    }
}
