package pos.repository;

import org.springframework.stereotype.Repository;
import pos.domain.TableSpot;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Repository
public class TableRepository {
  private final Map<Long, TableSpot> data = new HashMap<>();

  @PostConstruct
  void seed(){
    data.put(1L, new TableSpot(1L, "Mesa 1", 60, 60));
    data.put(2L, new TableSpot(2L, "Mesa 2", 180, 60));
    data.put(3L, new TableSpot(3L, "Mesa 3", 60, 160));
    data.put(4L, new TableSpot(4L, "Mesa 4", 180, 160));
  }

  public List<TableSpot> findAll(){ return new ArrayList<>(data.values()); }
  public Optional<TableSpot> findById(Long id){ return Optional.ofNullable(data.get(id)); }
}
