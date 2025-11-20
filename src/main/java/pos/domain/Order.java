package pos.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pedido")
  private Long id;

  @Builder.Default
  @Column(name = "fecha_pedido", nullable = false)
  private OffsetDateTime orderDate = OffsetDateTime.now();

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 30)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;

  // Cliente que originó el pedido
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_usuario_cliente", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User customer;

  // Dirección (nullable - solo para delivery)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_direccion")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Address address;

  // Sesión de mesa (nullable - solo dine-in)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_service_session")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private ServiceSession serviceSession;

  @DecimalMin(value = "0.0", inclusive = true)
  @Builder.Default
  @Column(name = "total", nullable = false, precision = 12, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "observacion", columnDefinition = "text")
  private String note;

  @Builder.Default
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt = OffsetDateTime.now();

  @PreUpdate
  void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}

