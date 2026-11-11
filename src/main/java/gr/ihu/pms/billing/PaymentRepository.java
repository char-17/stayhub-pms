package gr.ihu.pms.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceIdOrderByPaidAtAsc(Long invoiceId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoiceId = :invoiceId")
    BigDecimal totalPaid(@Param("invoiceId") Long invoiceId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paidAt between :from and :to")
    BigDecimal totalPaidBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
