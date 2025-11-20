package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "order_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long id;

    // FK → pedido.id_pedido
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @NotBlank
    @Column(name = "estado_anterior", nullable = false, length = 30)
    private String previousState;

    @NotBlank
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String newState;

    // FK → usuario.id_usuario (quem fez a mudança)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cambiado_por", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User changedBy;

    @Builder.Default
    @Column(name = "cambiado_en", nullable = false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    @Column(name = "nota", columnDefinition = "text")
    private String note;
}
