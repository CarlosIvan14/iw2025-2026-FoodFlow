package pos.service;

import org.springframework.stereotype.Service;
import pos.domain.OrderItem;
import pos.domain.Order;
import java.util.List;
import java.math.BigDecimal;

@Service
public class OrderService {

    public Order createCustomerOrder(Boolean delivery, String address, String phone, List<OrderItem> items, String username) {
        // Mock implementation
        return new Order(); 
    }

    public Order createTableOrder(Long tableId, List<OrderItem> items, String username) {
        // Mock implementation
        return new Order();
    }

    public List<Order> kitchenQueue() {
        return new java.util.ArrayList<>();
    }

    public List<Order> all() {
        return new java.util.ArrayList<>();
    }

    public void updateStatus(Long id, pos.domain.OrderStatus status) {
        // Mock
    }
}
