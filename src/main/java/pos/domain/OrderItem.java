package pos.domain;

import java.math.BigDecimal;

public record OrderItem(Long productId, String productName, Integer qty, BigDecimal unitPrice, String comment) {
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }
}
