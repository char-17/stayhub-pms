package gr.ihu.pms.rooms;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByNumberAsc();

    long countByStatus(RoomStatus status);

    long countByHousekeeping(HousekeepingStatus housekeeping);

    List<Room> findByHousekeepingNotOrderByNumberAsc(HousekeepingStatus housekeeping);

    List<Room> findByRoomTypeIdOrderByNumberAsc(Long roomTypeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
