package gr.ihu.pms.rooms;

import org.springframework.data.jpa.repository.JpaRepository;

/** Πρόσβαση στη βάση για τους τύπους δωματίων. */
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
}
