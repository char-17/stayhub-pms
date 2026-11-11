package gr.ihu.pms.billing;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillingService {

    private final InvoiceRepository invoices;
    private final PaymentRepository payments;

    public BillingService(InvoiceRepository invoices, PaymentRepository payments) {
        this.invoices = invoices;
        this.payments = payments;
    }

    @Transactional
    public Invoice issueForReservation(Long reservationId, Long guestId, String guestName,
                                       String roomNumber, int nights, List<InvoiceLineData> lines) {
        return invoices.findByReservationId(reservationId).orElseGet(() -> {
            Invoice invoice = new Invoice(reservationId, guestId, guestName, roomNumber, nights);
            for (InvoiceLineData line : lines) {
                invoice.addLine(line.description(), line.amount());
            }
            return invoices.save(invoice);
        });
    }

    @Transactional
    public void recordPayment(Long invoiceId, BigDecimal amount, String method) {
        Invoice invoice = invoices.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new NotFoundException("invoice", invoiceId));
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Payment amount must be positive");
        }
        BigDecimal balance = balanceOf(invoice);
        if (amount.compareTo(balance) > 0) {
            throw new BusinessRuleException("Payment exceeds the balance due (" + balance + ")");
        }
        payments.save(new Payment(invoiceId, amount, method == null || method.isBlank() ? "Cash" : method));
        invoice.applyPaid(payments.totalPaid(invoiceId));
    }

    public Invoice get(Long invoiceId) {
        return invoices.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("invoice", invoiceId));
    }

    public List<Payment> paymentsFor(Long invoiceId) {
        return payments.findByInvoiceIdOrderByPaidAtAsc(invoiceId);
    }

    public BigDecimal balanceOf(Invoice invoice) {
        return invoice.getAmount().subtract(payments.totalPaid(invoice.getId()));
    }

    public List<InvoiceRow> rows() {
        return toRows(invoices.findAllByOrderByIssuedAtDesc());
    }

    public List<InvoiceRow> rowsForGuest(Long guestId) {
        return toRows(invoices.findByGuestIdOrderByIssuedAtDesc(guestId));
    }

    public long countUnpaid() {
        return invoices.countByStatus(InvoiceStatus.UNPAID);
    }

    public BigDecimal invoicedBetween(LocalDate from, LocalDate to) {
        return invoices.revenueBetween(from.atStartOfDay(), to.atStartOfDay());
    }

    public BigDecimal collectedBetween(LocalDate from, LocalDate to) {
        return payments.totalPaidBetween(from.atStartOfDay(), to.atStartOfDay());
    }

    private List<InvoiceRow> toRows(List<Invoice> list) {
        return list.stream()
                .map(i -> {
                    BigDecimal paid = payments.totalPaid(i.getId());
                    return new InvoiceRow(i, paid, i.getAmount().subtract(paid));
                })
                .toList();
    }
}
