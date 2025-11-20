package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "table_spot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_table_spot")
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code; // código visible (ej. "M12")

    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity; // capacidad estimada

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    @Builder.Default
    private TableState state = TableState.FREE; // estado operativo

    @Column(name = "observacion", columnDefinition = "text")
    private String note; // nota interna
}
