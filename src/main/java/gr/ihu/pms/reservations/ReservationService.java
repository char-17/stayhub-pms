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

/** Λογική κρατήσεων και ενορχηστρωτής: συντονίζει rooms, guests, folio, billing και housekeeping. */
@Service
public class ReservationService {

    /** Οι καταστάσεις που δεσμεύουν δωμάτιο. */
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

    /** Διαθέσιμα δωμάτια ενός τύπου για τις ημερομηνίες. */
    public List<Room> availableRooms(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return reservations.findAvailableRooms(roomTypeId, checkIn, checkOut, ACTIVE);
    }

    /** Νέα κράτηση: κλειδώνει το δωμάτιο, ελέγχει επικάλυψη, υπολογίζει την τιμή. */
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

    /** Αλλαγή δωματίου ή ημερομηνιών, με τον ίδιο έλεγχο διαθεσιμότητας. */
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

    /** Check-in (ο κανόνας ελέγχεται μέσα στην οντότητα). */
    @Transactional
    public void checkIn(Long reservationId) {
        lock(reservationId).checkIn();
    }

    /** Check-out σε μία συναλλαγή: κράτηση → CHECKED_OUT, δωμάτιο → DIRTY, χρεώσεις, τιμολόγιο. */
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

    /** Ακύρωση κράτησης. */
    @Transactional
    public void cancel(Long reservationId) {
        lock(reservationId).cancel();
    }

    /**
     * Καταχωρεί πρόσθετη χρέωση (folio) σε μια κράτηση.
     * Κλειδώνει πρώτα την κράτηση, όπως και η αναχώρηση: έτσι μια χρέωση που καταχωρείται την
     * ίδια στιγμή με το check-out είτε προλαβαίνει το τιμολόγιο, είτε περιμένει, βλέπει
     * CHECKED_OUT και απορρίπτεται. Χωρίς το κλείδωμα θα μπορούσε να μείνει εκτός τιμολογίου.
     */
    @Transactional
    public Charge addCharge(Long reservationId, String description, BigDecimal amount) {
        // Ο κανόνας "μόνο σε ενεργή διαμονή" ελέγχεται μέσα στην οντότητα
        lock(reservationId).ensureAcceptsCharges();
        // Περιγραφή και ποσό τα επικυρώνει το module folio, μέσω της δημόσιας υπηρεσίας του
        return folio.addCharge(reservationId, description, amount);
    }

    /**
     * Οι κρατήσεις για τη σελίδα λίστας (νεότερες αφίξεις πρώτα), προαιρετικά μιας κατάστασης.
     * Υπάρχει ώστε ο controller να μη χρησιμοποιεί το repository: controller → υπηρεσία → repository.
     */
    public List<Reservation> list(ReservationStatus status) {
        return status == null
                ? reservations.findAllByOrderByCheckInDesc()
                : reservations.findByStatusOrderByCheckInDesc(status);
    }

    /** Οι κρατήσεις ενός πελάτη. */
    public List<Reservation> forGuest(Long guestId) {
        return reservations.findByGuestIdOrderByCheckInDesc(guestId);
    }

    /** Πόσες διαμονές είναι αυτή τη στιγμή σε εξέλιξη (CHECKED_IN). */
    public long countInHouse() {
        return reservations.countByStatus(ReservationStatus.CHECKED_IN);
    }

    /** Αναμενόμενες αφίξεις της ημέρας. */
    public List<Reservation> arrivalsOn(LocalDate date) {
        return reservations.findByStatusAndCheckIn(ReservationStatus.BOOKED, date);
    }

    /** Αναμενόμενες αναχωρήσεις της ημέρας. */
    public List<Reservation> departuresOn(LocalDate date) {
        return reservations.findByStatusAndCheckOut(ReservationStatus.CHECKED_IN, date);
    }

    /** Κρατήσεις μέσα στο διάστημα (για τις αναφορές). */
    public List<Reservation> soldBetween(LocalDate from, LocalDate to,
                                         Collection<ReservationStatus> sold) {
        return reservations.overlapping(from, to, sold);
    }

    /** Πλήθος αφίξεων στο διάστημα. */
    public long countArrivals(LocalDate from, LocalDate to) {
        return reservations.countArrivals(from, to);
    }

    /** Πλήθος αναχωρήσεων στο διάστημα. */
    public long countDepartures(LocalDate from, LocalDate to) {
        return reservations.countDepartures(from, to);
    }

    /** Τιμή = διανυκτερεύσεις × τιμή του τύπου δωματίου. */
    private BigDecimal price(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
    }

    /** Φέρνει την κράτηση κλειδωμένη, ή 404. */
    private Reservation lock(Long id) {
        return reservations.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("reservation", id));
    }

    /** Βρίσκει κράτηση ή πετά NotFoundException (σελίδα 404). */
    public Reservation get(Long id) {
        return reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("reservation", id));
    }

    /** Οι ημερομηνίες υπάρχουν, η αναχώρηση είναι μετά την άφιξη, η άφιξη όχι στο παρελθόν. */
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
