package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import pos.domain.Order;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(name = "fecha", nullable = false)
    @Builder.Default
    private OffsetDateTime date = OffsetDateTime.now(); // fecha

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total; // importe final cobrado

    // FK al usuario (staff que registró la venta)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // FK opcional al pedido (si proviene de uno)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;
}

