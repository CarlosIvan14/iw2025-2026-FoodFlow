package pos.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AnalyticsService {
  public Map<String, Double> salesByCategoryMock(){
    return Map.of("Comida", 320.0, "Bebida", 140.0);
  }
  public Map<String, Double> profitMock(){
    return Map.of("Hoy", 120.0, "Semana", 780.0, "Mes", 2950.0);
  }
}
