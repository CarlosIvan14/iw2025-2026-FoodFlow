package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.TableSpot;
import java.util.List;
import java.util.ArrayList;

@Service
public class TableService {

    public List<TableSpot> findAll() {
        return new ArrayList<>();
    }

    public List<TableSpot> all() {
        return findAll();
    }
}
