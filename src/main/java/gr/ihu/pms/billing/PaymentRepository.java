package gr.ihu.pms.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


//Τα ερωτήματα προς τη βάση
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Οι πληρωμές ενός τιμολογίου, με χρονολογική σειρά. */
    List<Payment> findByInvoiceIdOrderByPaidAtAsc(Long invoiceId);

    /** Άθροισμα πληρωμών ενός τιμολογίου (0 αν δεν υπάρχουν). */
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoiceId = :invoiceId")
    BigDecimal totalPaid(@Param("invoiceId") Long invoiceId);

    /** Άθροισμα όλων των πληρωμών στο διάστημα. */
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paidAt between :from and :to")
    BigDecimal totalPaidBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
