package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "producto") // O nome da tabela é 'producto'
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 1. Quando o repositório mandar deletar, o Hibernate vai executar este UPDATE:
@SQLDelete(sql = "UPDATE producto SET active = false WHERE id = ?")
// 2. Toda vez que buscar produtos, traz apenas os que active = true
@Where(clause = "active = true")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "descripcion", columnDefinition = "text")
    private String description;

    @NotBlank
    @Column(name = "categoria", length = 50)
    private String category;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    @Builder.Default
    private Integer stock = 0;

    // 3. O campo que controla se está ativo ou não
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}