package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.*;
import pos.repository.*;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional // Garante consistência: ou salva tudo (pedido + estoque) ou nada!
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceSessionService serviceSessionService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;

    public Order createCustomerOrder(Boolean delivery, String address, String phone, List<OrderItem> items, Long userId) {
        // Implementação futura para delivery...
        return new Order();
    }

    public Order createTableOrder(Long tableId, List<OrderItem> items, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

        ServiceSession session = serviceSessionService.findActiveSession(tableId)
                .orElseGet(() -> serviceSessionService.openSession(tableId, user.getId()));

        // Cálculo do total usando o novo método helper
        BigDecimal total = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Prepara o objeto Pedido
        Order order = Order.builder()
                .customer(user)
                .serviceSession(session)
                .status(OrderStatus.IN_PREPARATION)
                .total(total)
                .build();

        // --- LÓGICA DE ESTOQUE ---
        for (OrderItem item : items) {
            // Vincula o item ao pedido pai
            item.setOrder(order);

            // 1. Validar e Buscar o produto real no banco para pegar o estoque ATUAL
            // O frontend mandou o objeto Product, pegamos o ID dele.
            Long prodId = item.getProduct().getId();

            Product dbProduct = productRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado ID: " + prodId));

            // Atualizamos a referência do item para o produto gerenciado pelo Hibernate
            item.setProduct(dbProduct);

            int quantityToSell = item.getQuantity(); // Corrigido de getQty para getQuantity

            // 2. VERIFICAÇÃO: Tem estoque suficiente?
            if (dbProduct.getStock() < quantityToSell) {
                throw new RuntimeException("Estoque insuficiente para: " + dbProduct.getName()
                        + ". Disponível: " + dbProduct.getStock() + ", Solicitado: " + quantityToSell);
            }

            // 3. Baixar estoque
            dbProduct.setStock(dbProduct.getStock() - quantityToSell);
            productRepository.save(dbProduct);

            // 4. Registrar Movimento de Saída
            InventoryMovement movement = InventoryMovement.builder()
                    .product(dbProduct)
                    .quantity(quantityToSell)
                    .movementType(MovementType.EXIT)
                    .note("Venda Mesa " + tableId + " - Pedido #" + order.getId()) // ID do pedido só existe após salvar, cuidado aqui (pode ser null antes do flush)
                    .build();

            inventoryMovementRepository.save(movement);
        }

        order.setItems(items);

        // Salva o pedido final e propaga as alterações (Cascade)
        Order savedOrder = orderRepository.save(order);

        log.info("Pedido criado com sucesso: ID {}", savedOrder.getId());
        return savedOrder;
    }

    public List<Order> kitchenQueue() {
        return orderRepository.findByStatus(OrderStatus.IN_PREPARATION);
    }

    public List<Order> all() {
        // Certifique-se que existe este método no repositório ou use findAll()
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
     * NOVO MÉTODO DE PAGAMENTO COMPLETO
     * Substitui o antigo payOrder(id)
     */
    public void processPayment(Long orderId, PaymentMethod method, BigDecimal amountReceived, BigDecimal tip) {
        // 1. Busca o Pedido
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + orderId));

        // 2. Validação simples (Garante que não estamos pagando um pedido já pago)
        if (order.getStatus() == OrderStatus.PAGADO) {
            throw new IllegalStateException("Esta orden ya ha sido pagada.");
        }

        // 3. Cria a VENDA (Registro Financeiro)
        Sale sale = Sale.builder()
                .date(OffsetDateTime.now())
                .total(order.getTotal())
                .user(order.getCustomer())
                .order(order) // Link para rastreabilidade
                .build();

        saleRepository.save(sale); // Salva a venda primeiro para ter o ID

        // 4. Cria o PAGAMENTO vinculado à Venda
        // Nota: O 'amount' do pagamento é o valor da dívida (Total do Pedido).
        // O troco é calculado apenas visualmente no frontend e não precisa ser salvo aqui,
        // a menos que você queira auditar o fluxo de caixa físico.
        Payment payment = Payment.builder()
                .sale(sale)
                .method(method)
                .amount(order.getTotal())
                .tip(tip != null ? tip : BigDecimal.ZERO)
                .status(PaymentStatus.APPROVED)
                .createdAt(OffsetDateTime.now())
                .build();

        paymentRepository.save(payment);

        // 5. Atualiza o status do pedido para encerrar
        order.setStatus(OrderStatus.PAGADO);
        orderRepository.save(order);

        // Opcional: Se quiser fechar a sessão da mesa automaticamente
        // if (order.getServiceSession() != null) {
        //     serviceSessionService.closeSession(order.getServiceSession().getId());
        // }

        log.info("Pago procesado con éxito. Venda ID: {}, Pago ID: {}", sale.getId(), payment.getId());
    }

    public List<Order> findActiveOrdersByTable(Long tableId) {
        List<OrderStatus> finishedStatuses = List.of(OrderStatus.PAGADO, OrderStatus.CANCELED);
        return orderRepository.findByTableIdAndStatusNotIn(tableId, finishedStatuses);
    }
}