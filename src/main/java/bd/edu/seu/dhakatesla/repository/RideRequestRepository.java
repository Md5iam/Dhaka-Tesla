package bd.edu.seu.dhakatesla.repository;

import bd.edu.seu.dhakatesla.model.RidePool;
import bd.edu.seu.dhakatesla.model.RideRequest;
import bd.edu.seu.dhakatesla.model.RideStatus;
import bd.edu.seu.dhakatesla.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {
    List<RideRequest> findByPassengerOrderByRequestedAtDesc(User passenger);
    List<RideRequest> findByPassengerIdOrderByRequestedAtDesc(Long passengerId);
    List<RideRequest> findByPool(RidePool pool);
    List<RideRequest> findByPoolId(Long poolId);
    Optional<RideRequest> findFirstByPassengerIdAndStatusInOrderByRequestedAtDesc(Long passengerId, List<RideStatus> statuses);
}
