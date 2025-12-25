package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data // Use Data para Getter/Setter/Equals/Hash
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. REFERÊNCIA (O jeito que você quer):
    // Permite saber QUAL produto é, para relatórios e controle de estoque.
    // Graças ao Soft Delete que implementamos antes, isso aqui é seguro!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 2. SNAPSHOT (O jeito do seu amigo):
    // Cópia do nome no momento da venda.
    // Se o produto mudar de nome no futuro, o recibo antigo mantém o nome original.
    @Column(name = "product_name_snapshot", nullable = false)
    private String productName;

    // 3. SNAPSHOT DE PREÇO (Vital):
    // Nunca confie no preço atual do produto para pedidos passados.
    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    private String comment; // "Sem cebola", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude // Evita loop infinito no toString
    private Order order;

    // Método utilitário para calcular total da linha
    public BigDecimal getTotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}