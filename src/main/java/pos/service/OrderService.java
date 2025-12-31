// src/main/java/pos/service/OrderService.java
package pos.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.*;
import pos.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceSessionService serviceSessionService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;

    private final PdfService pdfService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, 
                       ServiceSessionService serviceSessionService,
                       UserRepository userRepository, 
                       ProductRepository productRepository,
                       InventoryMovementRepository inventoryMovementRepository, 
                       SaleRepository saleRepository,
                       PaymentRepository paymentRepository, 
                       PdfService pdfService, 
                       EmailService emailService) {
        this.orderRepository = orderRepository;
        this.serviceSessionService = serviceSessionService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.saleRepository = saleRepository;
        this.paymentRepository = paymentRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    public Order createCustomerOrder(Boolean delivery, String address, String phone, List<OrderItem> items, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

        // Cálculo del total
        BigDecimal total = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(user)
                .status(OrderStatus.IN_PREPARATION)
                .total(total)
                .build();

        // validar stock y decrementar
        for (OrderItem item : items) {
            item.setOrder(order);
            Long prodId = item.getProduct().getId();

            Product dbProduct = productRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado ID: " + prodId));

            item.setProduct(dbProduct);
            item.setProductName(dbProduct.getName());
            item.setUnitPrice(dbProduct.getPrice());

            int qty = item.getQuantity();
            if (dbProduct.getStock() < qty) {
                throw new RuntimeException("Estoque insuficiente para: " + dbProduct.getName());
            }

            dbProduct.setStock(dbProduct.getStock() - qty);
            productRepository.save(dbProduct);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(dbProduct)
                    .quantity(qty)
                    .movementType(MovementType.EXIT)
                    .note("Venta cliente - Pedido provisional")
                    .build();
            inventoryMovementRepository.save(movement);
        }

        order.setItems(items);
        Order saved = orderRepository.save(order);
        log.info("Pedido cliente creado con ID={}", saved.getId());
        return saved;
    }

    public Order createTableOrder(Long tableId, List<OrderItem> items, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

        ServiceSession session = serviceSessionService.findActiveSession(tableId)
                .orElseGet(() -> serviceSessionService.openSession(tableId, user.getId()));

        // Cálculo do total
        BigDecimal total = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(user)
                .serviceSession(session)
                .status(OrderStatus.PENDING)
                .total(total)
                .build();

        // --- LÓGICA DE ESTOQUE ---
        for (OrderItem item : items) {
            item.setOrder(order);

            Long prodId = item.getProduct().getId();

            Product dbProduct = productRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado ID: " + prodId));

            item.setProduct(dbProduct);

            int quantityToSell = item.getQuantity();

            if (dbProduct.getStock() < quantityToSell) {
                throw new RuntimeException("Estoque insuficiente para: " + dbProduct.getName()
                        + ". Disponível: " + dbProduct.getStock() + ", Solicitado: " + quantityToSell);
            }

            dbProduct.setStock(dbProduct.getStock() - quantityToSell);
            productRepository.save(dbProduct);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(dbProduct)
                    .quantity(quantityToSell)
                    .movementType(MovementType.EXIT)
                    .note("Venda Mesa " + tableId + " - Pedido (pendiente de ID)")
                    .build();

            inventoryMovementRepository.save(movement);
        }

        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        log.info("Pedido criado com sucesso: ID {}", savedOrder.getId());
        return savedOrder;
    }

    // ✅ Usa findKitchenQueue() que ya carga todo
    public List<Order> kitchenQueue() {
        return orderRepository.findKitchenQueue();
    }

    public List<Order> all() {
        return orderRepository.findAll();
    }

    public void updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + id));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", id, status);
    }

    public List<Order> readyToPayQueue() {
        return orderRepository.findByStatus(OrderStatus.LISTO);
    }

    /**
     * ✅ Para Analytics (evita LazyInitializationException):
     * trae pedidos con items+product inicializados dentro de la transacción.
     */
    @Transactional(readOnly = true)
    public List<Order> paidOrReadyWithItems(LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now();
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        // Ajusta la zona si tu createdAt está en OffsetDateTime (con offset real)
        ZoneId zone = ZoneId.systemDefault();

        OffsetDateTime startDt = start.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endDt = end.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        return orderRepository.findWithItemsBetween(
                startDt,
                endDt,
                List.of(OrderStatus.PAGADO, OrderStatus.LISTO)
        );
    }

    /**
     * NOVO MÉTODO DE PAGAMENTO COMPLETO
     */
    public void processPayment(Long orderId, PaymentMethod method, BigDecimal amountReceived, BigDecimal tip, String customerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + orderId));

        if (order.getStatus() == OrderStatus.PAGADO) {
            throw new IllegalStateException("Esta orden ya ha sido pagada.");
        }

        Sale sale = Sale.builder()
                .date(OffsetDateTime.now())
                .total(order.getTotal())
                .user(order.getCustomer())
                .order(order)
                .build();

        saleRepository.save(sale);

        Payment payment = Payment.builder()
                .sale(sale)
                .method(method)
                .amount(order.getTotal())
                .tip(tip != null ? tip : BigDecimal.ZERO)
                .status(PaymentStatus.APPROVED)
                .createdAt(OffsetDateTime.now())
                .build();

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAGADO);
        orderRepository.save(order);

        if (customerEmail != null && !customerEmail.isBlank()) {
            try {
                byte[] pdfBytes = pdfService.generateReceipt(order);
                emailService.sendReceiptWithPdf(customerEmail, pdfBytes, orderId);
                log.info("Processo de envio de recibo iniciado para: {}", customerEmail);
            } catch (Exception e) {
                log.error("Erro ao tentar enviar recibo por e-mail", e);
            }
        }

        log.info("Pago procesado con éxito. Venda ID: {}, Pago ID: {}", sale.getId(), payment.getId());
    }

    public List<Order> findActiveOrdersByTable(Long tableId) {
    List<OrderStatus> finishedStatuses = List.of(OrderStatus.PAGADO, OrderStatus.CANCELED);
    return orderRepository.findActiveByTableSpotIdAndStatusNotIn(tableId, finishedStatuses);
    }

    /**
     * Carga un pedido con los items inicializados (evita LazyInitializationException)
     */
    @Transactional(readOnly = true)
    public Order getOrderWithItemsInitialized(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + orderId));
        
        // Fuerza la inicialización de items dentro de la transacción
        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                // Acceder a propiedades para forzar carga
                item.getId();
                item.getProductName();
                item.getQuantity();
                item.getUnitPrice();
            });
        }
        
        return order;
    }

    /**
     * Agrega items a un pedido existente
     */
    @Transactional
    public void addItemsToOrder(Long orderId, List<OrderItem> newItems) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + orderId));

        BigDecimal addedTotal = BigDecimal.ZERO;

        // Procesar cada item nuevo
        for (OrderItem newItem : newItems) {
            Long prodId = newItem.getProduct().getId();

            Product dbProduct = productRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado ID: " + prodId));

            int quantityToAdd = newItem.getQuantity();

            // Verificar stock
            if (dbProduct.getStock() < quantityToAdd) {
                throw new RuntimeException("Estoque insuficiente para: " + dbProduct.getName()
                        + ". Disponível: " + dbProduct.getStock() + ", Solicitado: " + quantityToAdd);
            }

            // Decrementar stock
            dbProduct.setStock(dbProduct.getStock() - quantityToAdd);
            productRepository.save(dbProduct);

            // Registrar movimiento de inventario
            InventoryMovement movement = InventoryMovement.builder()
                    .product(dbProduct)
                    .quantity(quantityToAdd)
                    .movementType(MovementType.EXIT)
                    .note("Item adicional a Pedido #" + orderId)
                    .build();
            inventoryMovementRepository.save(movement);

            // Crear el item con el producto actualizado
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(dbProduct)
                    .productName(dbProduct.getName())
                    .unitPrice(dbProduct.getPrice())
                    .quantity(quantityToAdd)
                    .comment(newItem.getComment())
                    .build();

            order.getItems().add(item);

            // Acumular al total
            BigDecimal itemTotal = dbProduct.getPrice()
                    .multiply(BigDecimal.valueOf(quantityToAdd));
            addedTotal = addedTotal.add(itemTotal);
        }

        // Actualizar total del pedido
        BigDecimal currentTotal = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        order.setTotal(currentTotal.add(addedTotal));

        orderRepository.save(order);

        log.info("Items agregados al pedido #{}. Total nuevo: {}", orderId, order.getTotal());
    }

    /**
     * Crear pedido para llevar (sin mesa, para cliente)
     */
    public Order createCarryOutOrder(List<OrderItem> items, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        // Cálculo del total
        BigDecimal total = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(user)
                .serviceSession(null) // Para llevar, sin sesión de mesa
                .status(OrderStatus.PENDING)
                .total(total)
                .build();

        // --- LÓGICA DE ESTOQUE ---
        for (OrderItem item : items) {
            item.setOrder(order);

            Long prodId = item.getProduct().getId();

            Product dbProduct = productRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado ID: " + prodId));

            item.setProduct(dbProduct);

            int quantityToSell = item.getQuantity();

            if (dbProduct.getStock() < quantityToSell) {
                throw new RuntimeException("Stock insuficiente para: " + dbProduct.getName()
                        + ". Disponible: " + dbProduct.getStock() + ", Solicitado: " + quantityToSell);
            }

            dbProduct.setStock(dbProduct.getStock() - quantityToSell);
            productRepository.save(dbProduct);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(dbProduct)
                    .quantity(quantityToSell)
                    .movementType(MovementType.EXIT)
                    .note("Venta Para Llevar - Pedido (pendiente de ID)")
                    .build();

            inventoryMovementRepository.save(movement);
        }

        Order saved = orderRepository.save(order);
        log.info("Pedido para llevar creado #{}. Usuario: {}. Total: {}", saved.getId(), user.getEmail(), total);

        return saved;
    }

    /**
     * Buscar pedidos de un usuario (cliente)
     */
    public List<Order> findOrdersByUserId(String userId) {
        try {
            Long id = Long.parseLong(userId);
            return orderRepository.findAll().stream()
                    .filter(o -> o.getCustomer() != null && o.getCustomer().getId() != null && o.getCustomer().getId().equals(id))
                    .toList();
        } catch (NumberFormatException e) {
            return List.of();
        }
    }
}