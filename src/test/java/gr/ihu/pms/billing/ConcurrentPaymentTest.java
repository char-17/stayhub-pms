package gr.ihu.pms.billing;

import gr.ihu.pms.Concurrently;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrentPaymentTest {

    @Autowired BillingService billing;
    @Autowired InvoiceRepository invoices;
    @Autowired PaymentRepository payments;

    Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = billing.issueForReservation(9001L, 1L, "Grace Hopper", "C-101", 2,
                List.of(new InvoiceLineData("Room C-101 · 2 nights", new BigDecimal("230.00"))));
    }

    @AfterEach
    void tearDown() {
        payments.deleteAll(payments.findByInvoiceIdOrderByPaidAtAsc(invoice.getId()));
        invoices.deleteById(invoice.getId());
    }

    @Test
    void concurrentPaymentsCannotExceedTheBalance() throws Exception {
        Concurrently.Result result = Concurrently.run(5, () -> {
            billing.recordPayment(invoice.getId(), new BigDecimal("130.00"), "Cash");
            return null;
        });

        assertThat(result.successes()).as("payments accepted").isEqualTo(1);
        assertThat(payments.totalPaid(invoice.getId())).isEqualByComparingTo("130.00");
    }
}
