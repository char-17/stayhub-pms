package gr.ihu.pms.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
// Collections.unmodifiableList: οι γραμμές δίνονται προς τα έξω μόνο για ανάγνωση
import java.util.Collections;
import java.util.List;

/**
 * Τιμολόγιο: στιγμιότυπο της διαμονής τη στιγμή του check-out.
 * Κρατά μόνο τον αριθμό της κράτησης (όχι σχέση) και αντίγραφα των στοιχείων της.
 */
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

//    Προσθέτει τη γραμμή και ενημερώνει το σύνολο, στο ίδιο βήμα.
//    Το σύνολο δεν μπορεί ποτέ να διαφωνεί με το άθροισμα των γραμμών
    public Invoice(Long reservationId, Long guestId, String guestName, String roomNumber, int nights) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.nights = nights;
    }

    /** Προσθέτει γραμμή και αυξάνει το σύνολο κατά το ποσό της. */
    public void addLine(String description, BigDecimal lineAmount) {
        lines.add(new InvoiceLine(this, description, lineAmount));
        this.amount = this.amount.add(lineAmount);
    }

    /** Ορίζει την κατάσταση (UNPAID / PARTIAL / PAID) από το συνολικό πληρωμένο ποσό. */
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

    /**
     * Οι γραμμές, μόνο για ανάγνωση. Γραμμή προστίθεται μόνο με την addLine, που ενημερώνει
     * και το σύνολο· έτσι το ποσό δεν μπορεί να διαφωνήσει με το άθροισμα των γραμμών.
     */
    public List<InvoiceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
