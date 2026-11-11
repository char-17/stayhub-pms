package gr.ihu.pms.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false)
    private Long guestId;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private int nights;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceLine> lines = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(Long reservationId, Long guestId, String guestName, String roomNumber, int nights) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.nights = nights;
    }

    public void addLine(String description, BigDecimal lineAmount) {
        lines.add(new InvoiceLine(this, description, lineAmount));
        this.amount = this.amount.add(lineAmount);
    }

    public void applyPaid(BigDecimal paid) {
        if (paid.compareTo(amount) >= 0) {
            status = InvoiceStatus.PAID;
        } else if (paid.signum() > 0) {
            status = InvoiceStatus.PARTIAL;
        } else {
            status = InvoiceStatus.UNPAID;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public int getNights() {
        return nights;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }
}
