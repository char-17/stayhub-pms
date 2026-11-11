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

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByIssuedAtDesc();

    Optional<Invoice> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

    List<Invoice> findByGuestIdOrderByIssuedAtDesc(Long guestId);

    long countByStatus(InvoiceStatus status);

    @Query("select coalesce(sum(i.amount), 0) from Invoice i where i.issuedAt between :from and :to")
    BigDecimal revenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);
}
