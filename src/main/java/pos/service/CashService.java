package pos.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CashService {

    public record CashReport(BigDecimal cashIn, BigDecimal cashOut, BigDecimal balance) {}

    public CashReport closeDayMock() {
        // Mock data
        return new CashReport(
            new BigDecimal("1500.00"), 
            new BigDecimal("300.00"), 
            new BigDecimal("1200.00")
        );
    }
}
