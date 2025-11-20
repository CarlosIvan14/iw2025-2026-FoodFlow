package pos.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

import pos.domain.SessionState;

@Entity
@Table(name = "service_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service_session")
    private Long id;

    // FK → table_spot.id_table_spot
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_table_spot", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TableSpot tableSpot;

    // FK → usuario.id_usuario (mesero)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_mesero", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User waiter;

    @Builder.Default
    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt = OffsetDateTime.now();

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private SessionState state = SessionState.OPEN;

    // helper method
    public boolean isOpen() {
        return this.state == SessionState.OPEN && this.closedAt == null;
    }

    public void close() {
        this.state = SessionState.CLOSED;
        this.closedAt = OffsetDateTime.now();
    }
}

