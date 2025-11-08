package pos.service;

import org.springframework.stereotype.Service;

@Service
public class CashService {
  public record CashCut(double cashIn, double cashOut, double balance) {}
  public CashCut closeDayMock(){ return new CashCut(850.0, 120.0, 730.0); }
}
