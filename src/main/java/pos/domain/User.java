package pos.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "usuario")
@Data                   // getters, setters, toString, equals/hashCode
@NoArgsConstructor       // construtor vazio (necessário pro JPA)
@AllArgsConstructor      // construtor com todos os campos
@Builder                 // padrão Builder opcional
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "contraseña", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Role role;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean active = true;



    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Address> addresses = new java.util.ArrayList<>();

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return passwordHash;
    }

    public boolean getActive() {
        return active;
    }


}
