package gr.ihu.pms.reservations;

import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.billing.InvoiceLineData;
import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.common.NotFoundException;
import gr.ihu.pms.folio.Charge;
import gr.ihu.pms.folio.FolioService;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.guests.GuestService;
import gr.ihu.pms.housekeeping.HousekeepingService;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ReservationService {

    public static final List<ReservationStatus> ACTIVE =
            List.of(ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservations;
    private final GuestService guests;
    private final RoomService rooms;
    private final BillingService billing;
    private final FolioService folio;
    private final HousekeepingService housekeeping;

    public ReservationService(ReservationRepository reservations, GuestService guests,
                              RoomService rooms, BillingService billing, FolioService folio,
                              HousekeepingService housekeeping) {
        this.reservations = reservations;
        this.guests = guests;
        this.rooms = rooms;
        this.billing = billing;
        this.folio = folio;
        this.housekeeping = housekeeping;
    }

    public List<Room> availableRooms(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return reservations.findAvailableRooms(roomTypeId, checkIn, checkOut, ACTIVE);
    }

    @Transactional
    public Reservation book(Long guestId, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        Guest guest = guests.get(guestId);
        Room room = rooms.lockForBooking(roomId);
        if (reservations.hasOverlap(roomId, checkIn, checkOut, ACTIVE)) {
            throw new BusinessRuleException("Room " + room.getNumber() + " is not available for those dates");
        }
        return reservations.save(new Reservation(guest, room, checkIn, checkOut, price(room, checkIn, checkOut)));
    }

    @Transactional
    public Reservation reschedule(Long reservationId, Long newRoomId, LocalDate checkIn, LocalDate checkOut) {
        Reservation r = lock(reservationId);
        r.ensureCanBeModified();
        validateDates(checkIn, checkOut);
        Room room = rooms.lockForBooking(newRoomId);
        if (reservations.hasOverlapExcluding(newRoomId, checkIn, checkOut, ACTIVE, reservationId)) {
            throw new BusinessRuleException("Room " + room.getNumber() + " is not available for those dates");
        }
        r.reschedule(room, checkIn, checkOut, price(room, checkIn, checkOut));
        return r;
    }

    @Transactional
    public void checkIn(Long reservationId) {
        lock(reservationId).checkIn();
    }

    @Transactional
    public void checkOut(Long reservationId) {
        Reservation r = lock(reservationId);
        r.checkOut();
        housekeeping.roomVacated(r.getRoom().getId());

        List<InvoiceLineData> lines = new ArrayList<>();
        lines.add(new InvoiceLineData(
                "Room " + r.getRoom().getNumber() + " · " + r.nights() + " night(s)", r.getTotalPrice()));
        for (Charge c : folio.forReservation(r.getId())) {
            lines.add(new InvoiceLineData(c.getDescription(), c.getAmount()));
        }
        billing.issueForReservation(r.getId(), r.getGuest().getId(), r.getGuest().fullName(),
                r.getRoom().getNumber(), (int) r.nights(), lines);
    }

    @Transactional
    public void cancel(Long reservationId) {
        lock(reservationId).cancel();
    }

    public List<Reservation> forGuest(Long guestId) {
        return reservations.findByGuestIdOrderByCheckInDesc(guestId);
    }

    public long countInHouse() {
        return reservations.countByStatus(ReservationStatus.CHECKED_IN);
    }

    public List<Reservation> arrivalsOn(LocalDate date) {
        return reservations.findByStatusAndCheckIn(ReservationStatus.BOOKED, date);
    }

    public List<Reservation> departuresOn(LocalDate date) {
        return reservations.findByStatusAndCheckOut(ReservationStatus.CHECKED_IN, date);
    }

    public List<Reservation> soldBetween(LocalDate from, LocalDate to,
                                         Collection<ReservationStatus> sold) {
        return reservations.overlapping(from, to, sold);
    }

    public long countArrivals(LocalDate from, LocalDate to) {
        return reservations.countArrivals(from, to);
    }

    public long countDepartures(LocalDate from, LocalDate to) {
        return reservations.countDepartures(from, to);
    }

    private BigDecimal price(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
    }

    private Reservation lock(Long id) {
        return reservations.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("reservation", id));
    }

    public Reservation get(Long id) {
        return reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("reservation", id));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BusinessRuleException("Check-in and check-out dates are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new BusinessRuleException("Check-out must be after check-in");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Check-in cannot be in the past");
        }
    }
}
