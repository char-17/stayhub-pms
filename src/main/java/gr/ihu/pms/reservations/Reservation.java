package gr.ihu.pms.reservations;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.rooms.Room;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Κράτηση: πελάτης, δωμάτιο, ημερομηνίες, τιμή και κατάσταση.
 * Οι κανόνες του κύκλου ζωής βρίσκονται εδώ:
 * BOOKED → CHECKED_IN → CHECKED_OUT, ή BOOKED → CANCELLED.
 */
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Guest guest;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.BOOKED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Reservation() {
    }

    public Reservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, BigDecimal totalPrice) {
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
    }

    /** Πλήθος διανυκτερεύσεων. */
    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    /** Άφιξη: επιτρέπεται μόνο από BOOKED. */
    public void checkIn() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be checked in");
        status = ReservationStatus.CHECKED_IN;
    }

    /** Αναχώρηση: επιτρέπεται μόνο από CHECKED_IN. */
    public void checkOut() {
        require(ReservationStatus.CHECKED_IN, "Only a checked-in reservation can be checked out");
        status = ReservationStatus.CHECKED_OUT;
    }

    /** Ακύρωση: επιτρέπεται μόνο από BOOKED. */
    public void cancel() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be cancelled");
        status = ReservationStatus.CANCELLED;
    }

    /** Ελέγχει ότι η κράτηση μπορεί ακόμη να αλλάξει (είναι BOOKED). */
    public void ensureCanBeModified() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be modified");
    }

    /**
     * Ελέγχει ότι η κράτηση δέχεται ακόμη χρεώσεις folio: μόνο όσο η διαμονή είναι ενεργή
     * (BOOKED ή CHECKED_IN). Μετά την αναχώρηση το τιμολόγιο έχει ήδη εκδοθεί.
     * Ο κανόνας βρισκόταν στον controller· μεταφέρθηκε εδώ ώστε να ισχύει για όλους.
     */
    public void ensureAcceptsCharges() {
        if (status != ReservationStatus.BOOKED && status != ReservationStatus.CHECKED_IN) {
            throw new BusinessRuleException("Charges can only be posted to an active stay");
        }
    }

    /** Πετά BusinessRuleException αν η κατάσταση δεν είναι η αναμενόμενη. */
    private void require(ReservationStatus expected, String message) {
        if (status != expected) {
            throw new BusinessRuleException(message);
        }
    }

    /** Αλλάζει δωμάτιο, ημερομηνίες και τιμή· μόνο σε BOOKED. */
    public void reschedule(Room room, LocalDate checkIn, LocalDate checkOut, BigDecimal totalPrice) {
        ensureCanBeModified();
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
