package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    // FK → producto.id_producto
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private MovementType movementType;

    @Min(1)
    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(name = "fecha_movimiento", nullable = false)
    private OffsetDateTime movementDate = OffsetDateTime.now();

    @Column(name = "observacion", columnDefinition = "text")
    private String note;
}

