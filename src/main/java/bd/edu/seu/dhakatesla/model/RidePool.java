package bd.edu.seu.dhakatesla.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_pools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RidePool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PoolStatus status = PoolStatus.OPEN;

    @Column(nullable = false)
    @Builder.Default
    private int totalCapacity = 3;

    @Column(nullable = false)
    @Builder.Default
    private int occupiedSeats = 0;

    @Column(nullable = false)
    private String startZone;

    @Column(nullable = false)
    private String destinationCorridor;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
