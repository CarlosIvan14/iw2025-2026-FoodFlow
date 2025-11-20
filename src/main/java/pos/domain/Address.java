package pos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "direccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @NotBlank
    @Size(max = 120)
    @Column(name = "calle", nullable = false, length = 120)
    private String street;          // calle

    @Size(max = 20)
    @Column(name = "numero", length = 20)
    private String number;          // numero (número, km, lote, etc.)

    @Size(max = 120)
    @Column(name = "complemento", length = 120)
    private String complement;      // complemento (depto, piso, ref. interna)

    @NotBlank
    @Size(max = 80)
    @Column(name = "ciudad", nullable = false, length = 80)
    private String city;            // ciudad o municipio

    @Size(max = 80)
    @Column(name = "provincia", length = 80)
    private String state;           // provincia/estado

    @Size(max = 20)
    @Column(name = "codigo_postal", length = 20)
    private String postalCode;      // código postal

    @NotBlank
    @Size(max = 60)
    @Column(name = "pais", nullable = false, length = 60)
    private String country;         // país

    @Column(name = "referencia", columnDefinition = "text")
    private String reference;       // punto de referencia para entrega
}

