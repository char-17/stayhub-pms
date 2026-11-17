package gr.ihu.pms.rooms;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Πρόσβαση στη βάση για τα δωμάτια. */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** Όλα τα δωμάτια, με σειρά αριθμού. */
    List<Room> findAllByOrderByNumberAsc();

    /** Πλήθος δωματίων σε μια κατάσταση λειτουργίας. */
    long countByStatus(RoomStatus status);

    /** Πλήθος δωματίων σε μια κατάσταση καθαριότητας. */
    long countByHousekeeping(HousekeepingStatus housekeeping);

    /** Τα δωμάτια που ΔΕΝ είναι στη δοσμένη κατάσταση καθαριότητας. */
    List<Room> findByHousekeepingNotOrderByNumberAsc(HousekeepingStatus housekeeping);

    /** Τα δωμάτια ενός τύπου. */
    List<Room> findByRoomTypeIdOrderByNumberAsc(Long roomTypeId);

    /** Φέρνει το δωμάτιο κλειδώνοντας τη γραμμή του (SELECT ... FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
