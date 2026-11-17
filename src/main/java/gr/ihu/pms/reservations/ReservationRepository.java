package gr.ihu.pms.reservations;

import gr.ihu.pms.rooms.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Πρόσβαση στη βάση για τις κρατήσεις, μαζί με τα ερωτήματα διαθεσιμότητας. */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Φέρνει την κράτηση κλειδώνοντας τη γραμμή της (SELECT ... FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    /** Διαθέσιμα δωμάτια ενός τύπου: σε λειτουργία, όχι DIRTY, χωρίς ενεργή κράτηση που επικαλύπτεται. */
    @Query("""
        select r from Room r
        where r.roomType.id = :typeId
          and r.status = gr.ihu.pms.rooms.RoomStatus.AVAILABLE
          and r.housekeeping <> gr.ihu.pms.rooms.HousekeepingStatus.DIRTY
          and r.id not in (
              select res.room.id from Reservation res
              where res.status in :active
                and res.checkIn < :out and res.checkOut > :in)
        order by r.number
        """)
    List<Room> findAvailableRooms(@Param("typeId") Long typeId,
                                  @Param("in") LocalDate in,
                                  @Param("out") LocalDate out,
                                  @Param("active") Collection<ReservationStatus> active);

    /**
     * Υπάρχει ενεργή κράτηση του δωματίου που επικαλύπτεται;
     * Ημι-ανοιχτά διαστήματα: < και >, όχι <=, ώστε αναχώρηση και άφιξη την ίδια μέρα να μη συγκρούονται.
     */
    @Query("""
        select count(res) > 0 from Reservation res
        where res.room.id = :roomId and res.status in :active
          and res.checkIn < :out and res.checkOut > :in
        """)
    boolean hasOverlap(@Param("roomId") Long roomId,
                       @Param("in") LocalDate in,
                       @Param("out") LocalDate out,
                       @Param("active") Collection<ReservationStatus> active);

    /** Ίδιος έλεγχος, εξαιρώντας μία κράτηση (για την αλλαγή ημερομηνιών). */
    @Query("""
        select count(res) > 0 from Reservation res
        where res.room.id = :roomId and res.id <> :excludeId and res.status in :active
          and res.checkIn < :out and res.checkOut > :in
        """)
    boolean hasOverlapExcluding(@Param("roomId") Long roomId,
                                @Param("in") LocalDate in,
                                @Param("out") LocalDate out,
                                @Param("active") Collection<ReservationStatus> active,
                                @Param("excludeId") Long excludeId);

    /** Κρατήσεις μιας κατάστασης με άφιξη τη δοσμένη ημέρα. */
    @Query("select r from Reservation r where r.status = :status and r.checkIn = :date")
    List<Reservation> findByStatusAndCheckIn(@Param("status") ReservationStatus status,
                                             @Param("date") LocalDate date);

    /** Κρατήσεις μιας κατάστασης με αναχώρηση τη δοσμένη ημέρα. */
    @Query("select r from Reservation r where r.status = :status and r.checkOut = :date")
    List<Reservation> findByStatusAndCheckOut(@Param("status") ReservationStatus status,
                                              @Param("date") LocalDate date);

    /** Όλες οι κρατήσεις, οι νεότερες αφίξεις πρώτα. */
    List<Reservation> findAllByOrderByCheckInDesc();

    /** Οι κρατήσεις μιας κατάστασης. */
    List<Reservation> findByStatusOrderByCheckInDesc(ReservationStatus status);

    /** Οι κρατήσεις ενός πελάτη. */
    List<Reservation> findByGuestIdOrderByCheckInDesc(Long guestId);

    /** Πλήθος κρατήσεων σε μια κατάσταση. */
    long countByStatus(ReservationStatus status);

    /** Κρατήσεις που πέφτουν μέσα στο διάστημα (για τις αναφορές). */
    @Query("""
        select r from Reservation r
        where r.status in :active and r.checkIn < :to and r.checkOut > :from
        """)
    List<Reservation> overlapping(@Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  @Param("active") Collection<ReservationStatus> active);

    /** Πλήθος αφίξεων στο διάστημα, χωρίς τις ακυρωμένες. */
    @Query("""
        select count(r) from Reservation r
        where r.status <> gr.ihu.pms.reservations.ReservationStatus.CANCELLED
          and r.checkIn >= :from and r.checkIn < :to
        """)
    long countArrivals(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Πλήθος αναχωρήσεων στο διάστημα, χωρίς τις ακυρωμένες. */
    @Query("""
        select count(r) from Reservation r
        where r.status <> gr.ihu.pms.reservations.ReservationStatus.CANCELLED
          and r.checkOut >= :from and r.checkOut < :to
        """)
    long countDepartures(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
