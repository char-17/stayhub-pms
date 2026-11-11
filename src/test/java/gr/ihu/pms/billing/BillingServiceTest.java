package gr.ihu.pms.billing;

import gr.ihu.pms.common.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BillingServiceTest {

    @Autowired BillingService billing;
    @Autowired InvoiceRepository invoices;

    private Invoice issue(long reservationId) {
        return billing.issueForReservation(reservationId, 1L, "Ada Lovelace", "101", 2,
                List.of(new InvoiceLineData("Room 101 · 2 nights", new BigDecimal("200.00")),
                        new InvoiceLineData("Breakfast", new BigDecimal("30.00"))));
    }

    @Test
    void invoiceAmountIsSumOfLines() {
        Invoice inv = issue(1001L);
        assertThat(inv.getAmount()).isEqualByComparingTo("230.00");
        assertThat(inv.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
    }

    @Test
    void issuingTwiceForSameReservationIsIdempotent() {
        Invoice a = issue(1002L);
        Invoice b = issue(1002L);
        assertThat(b.getId()).isEqualTo(a.getId());
    }

    @Test
    void partialThenFullPaymentUpdatesStatusAndBalance() {
        Invoice inv = issue(1003L);
        billing.recordPayment(inv.getId(), new BigDecimal("100.00"), "Cash");
        Invoice afterPartial = invoices.findById(inv.getId()).orElseThrow();
        assertThat(afterPartial.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
        assertThat(billing.balanceOf(afterPartial)).isEqualByComparingTo("130.00");

        billing.recordPayment(inv.getId(), new BigDecimal("130.00"), "Card");
        Invoice afterFull = invoices.findById(inv.getId()).orElseThrow();
        assertThat(afterFull.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(billing.balanceOf(afterFull)).isEqualByComparingTo("0.00");
    }

    @Test
    void overpaymentIsRejected() {
        Invoice inv = issue(1004L);
        assertThatThrownBy(() -> billing.recordPayment(inv.getId(), new BigDecimal("500.00"), "Cash"))
                .isInstanceOf(BusinessRuleException.class);
    }
}
