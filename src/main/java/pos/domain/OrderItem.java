package pos.domain;

import java.time.Instant;
import java.util.List;

public record OrderItem(Long productId, String name, int qty, double unitPrice, String note) {}
