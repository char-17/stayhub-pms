package gr.ihu.pms.billing;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


//Τα ερωτήματα προς τη βάση

/** Πρόσβαση στη βάση για τα τιμολόγια. Χρησιμοποιείται μόνο από το BillingService. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Όλα τα τιμολόγια, τα νεότερα πρώτα. */
    List<Invoice> findAllByOrderByIssuedAtDesc();

    /** Το τιμολόγιο μιας κράτησης, αν υπάρχει. */
    Optional<Invoice> findByReservationId(Long reservationId);

    /** Υπάρχει τιμολόγιο για την κράτηση; */
    boolean existsByReservationId(Long reservationId);

    /** Τα τιμολόγια ενός πελάτη, τα νεότερα πρώτα. */
    List<Invoice> findByGuestIdOrderByIssuedAtDesc(Long guestId);

    /** Πλήθος τιμολογίων σε μια κατάσταση. */
    long countByStatus(InvoiceStatus status);

    /** Άθροισμα ποσών των τιμολογίων που εκδόθηκαν στο διάστημα. */
    @Query("select coalesce(sum(i.amount), 0) from Invoice i where i.issuedAt between :from and :to")
    BigDecimal revenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Φέρνει το τιμολόγιο κλειδώνοντας τη γραμμή του (SELECT ... FOR UPDATE). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);
}
