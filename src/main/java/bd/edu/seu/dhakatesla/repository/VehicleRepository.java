package bd.edu.seu.dhakatesla.repository;

import bd.edu.seu.dhakatesla.model.User;
import bd.edu.seu.dhakatesla.model.Vehicle;
import bd.edu.seu.dhakatesla.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByDriver(User driver);
    Optional<Vehicle> findByDriverId(Long driverId);
    List<Vehicle> findByStatus(VehicleStatus status);
}
