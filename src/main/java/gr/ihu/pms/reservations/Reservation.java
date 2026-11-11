package gr.ihu.pms.reservations;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.rooms.Room;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    public void checkIn() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be checked in");
        status = ReservationStatus.CHECKED_IN;
    }

    public void checkOut() {
        require(ReservationStatus.CHECKED_IN, "Only a checked-in reservation can be checked out");
        status = ReservationStatus.CHECKED_OUT;
    }

    public void cancel() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be cancelled");
        status = ReservationStatus.CANCELLED;
    }

    public void ensureCanBeModified() {
        require(ReservationStatus.BOOKED, "Only a booked reservation can be modified");
    }

    private void require(ReservationStatus expected, String message) {
        if (status != expected) {
            throw new BusinessRuleException(message);
        }
    }

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
