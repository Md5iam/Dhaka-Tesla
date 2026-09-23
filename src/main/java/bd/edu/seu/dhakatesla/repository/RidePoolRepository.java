package bd.edu.seu.dhakatesla.repository;

import bd.edu.seu.dhakatesla.model.PoolStatus;
import bd.edu.seu.dhakatesla.model.RidePool;
import bd.edu.seu.dhakatesla.model.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RidePoolRepository extends JpaRepository<RidePool, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM RidePool p WHERE p.id = :id")
    Optional<RidePool> findByIdWithLock(@Param("id") Long id);

    List<RidePool> findByStatusAndStartZone(PoolStatus status, String startZone);

    Optional<RidePool> findByVehicleAndStatusIn(Vehicle vehicle, List<PoolStatus> statuses);

    Optional<RidePool> findFirstByVehicleDriverIdAndStatusIn(Long driverId, List<PoolStatus> statuses);
}
