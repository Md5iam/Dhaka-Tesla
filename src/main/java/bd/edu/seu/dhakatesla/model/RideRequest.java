package bd.edu.seu.dhakatesla.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pool_id")
    private RidePool pool;

    @Column(nullable = false)
    private String pickupZone;

    @Column(nullable = false)
    private String destinationZone;

    @Column(nullable = false)
    @Builder.Default
    private int requestedSeats = 1;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceCharge;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal poolDiscount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideStatus status = RideStatus.REQUESTED;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime completedAt;
}
