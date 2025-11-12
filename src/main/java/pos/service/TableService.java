package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.TableSpot;

import java.util.List;

@Service
public class TableService {
  private final TableRepository repo;
  public TableService(TableRepository repo){ this.repo = repo; }
  public List<TableSpot> all(){ return repo.findAll(); }
}
