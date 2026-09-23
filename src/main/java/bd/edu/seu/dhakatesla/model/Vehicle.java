package bd.edu.seu.dhakatesla.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    @Builder.Default
    private int capacity = 3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ONLINE;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", unique = true)
    private User driver;
}
