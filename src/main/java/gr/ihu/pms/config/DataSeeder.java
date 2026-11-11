package gr.ihu.pms.config;

import gr.ihu.pms.auth.Role;
import gr.ihu.pms.auth.User;
import gr.ihu.pms.auth.UserRepository;
import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.billing.InvoiceLineData;
import gr.ihu.pms.billing.Invoice;
import gr.ihu.pms.folio.Charge;
import gr.ihu.pms.folio.ChargeRepository;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.guests.GuestRepository;
import gr.ihu.pms.reservations.Reservation;
import gr.ihu.pms.reservations.ReservationRepository;
import gr.ihu.pms.reservations.ReservationStatus;
import gr.ihu.pms.rooms.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "pms", name = "demo-data", havingValue = "true")
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final RoomTypeRepository roomTypes;
    private final RoomRepository rooms;
    private final GuestRepository guests;
    private final ReservationRepository reservations;
    private final ChargeRepository charges;
    private final BillingService billing;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, RoomTypeRepository roomTypes, RoomRepository rooms,
                      GuestRepository guests, ReservationRepository reservations,
                      ChargeRepository charges, BillingService billing, PasswordEncoder encoder) {
        this.users = users;
        this.roomTypes = roomTypes;
        this.rooms = rooms;
        this.guests = guests;
        this.reservations = reservations;
        this.charges = charges;
        this.billing = billing;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() == 0) {
            users.save(new User("manager", encoder.encode("manager"), "Hotel Manager", Role.MANAGER));
            users.save(new User("reception", encoder.encode("reception"), "Front Desk", Role.RECEPTIONIST));
        }

        if (roomTypes.count() == 0) {
            RoomType single = roomTypes.save(new RoomType("Single", new BigDecimal("60.00"), 1, "One single bed."));
            RoomType dbl = roomTypes.save(new RoomType("Double", new BigDecimal("90.00"), 2, "One double bed."));
            RoomType suite = roomTypes.save(new RoomType("Suite", new BigDecimal("150.00"), 4, "Living room and bedroom."));

            Room r101 = rooms.save(new Room("101", single, 1));
            Room r102 = rooms.save(new Room("102", single, 1));
            Room r103 = rooms.save(new Room("103", dbl, 1));
            Room r104 = rooms.save(new Room("104", dbl, 1));
            Room r105 = rooms.save(new Room("105", dbl, 1));
            Room r201 = rooms.save(new Room("201", dbl, 2));
            Room r202 = rooms.save(new Room("202", dbl, 2));
            Room r203 = rooms.save(new Room("203", suite, 2));
            Room r301 = rooms.save(new Room("301", suite, 3));
            Room r302 = rooms.save(new Room("302", suite, 3));

            r105.takeOutOfService();
            rooms.save(r105);

            Guest maria = saveGuest("Maria", "Papadopoulou", "maria.p@example.com", "+30 210 1234567", null);
            Guest john = saveGuest("John", "Smith", "john.smith@example.com", "+44 20 7946 0000", "AB123456");
            Guest elena = saveGuest("Elena", "Nikolaou", "elena.nik@example.com", "+30 2310 567890", null);
            Guest hans = saveGuest("Hans", "Mueller", "hans.m@example.de", "+49 89 1234567", "DE9876543");
            Guest sofia = saveGuest("Sofia", "Karagianni", "sofia.k@example.com", "+30 697 1234567", null);
            Guest pierre = saveGuest("Pierre", "Dupont", "pierre.d@example.fr", "+33 1 2345 6789", "FR7654321");
            Guest dimitris = saveGuest("Dimitris", "Alexiou", "d.alexiou@example.com", "+30 694 9876543", null);
            Guest anna = saveGuest("Anna", "Petridou", "anna.pet@example.com", "+30 210 9876543", null);

            LocalDate today = LocalDate.now();

            Reservation res1 = createReservation(maria, r103, today.minusDays(15), today.minusDays(10));
            res1.checkIn();
            res1.checkOut();
            reservations.save(res1);
            charges.save(new Charge(res1.getId(), "Breakfast (5 days)", new BigDecimal("50.00")));
            charges.save(new Charge(res1.getId(), "Minibar", new BigDecimal("18.00")));
            Invoice inv1 = issueInvoice(res1, List.of(
                    new InvoiceLineData("Room 103 · 5 night(s)", new BigDecimal("450.00")),
                    new InvoiceLineData("Breakfast (5 days)", new BigDecimal("50.00")),
                    new InvoiceLineData("Minibar", new BigDecimal("18.00"))
            ));
            billing.recordPayment(inv1.getId(), new BigDecimal("518.00"), "Card");

            Reservation res2 = createReservation(john, r203, today.minusDays(8), today.minusDays(5));
            res2.checkIn();
            res2.checkOut();
            reservations.save(res2);
            charges.save(new Charge(res2.getId(), "Spa Treatment", new BigDecimal("45.00")));
            charges.save(new Charge(res2.getId(), "Room Service Dinner", new BigDecimal("15.00")));
            Invoice inv2 = issueInvoice(res2, List.of(
                    new InvoiceLineData("Room 203 · 3 night(s)", new BigDecimal("450.00")),
                    new InvoiceLineData("Spa Treatment", new BigDecimal("45.00")),
                    new InvoiceLineData("Room Service Dinner", new BigDecimal("15.00"))
            ));
            billing.recordPayment(inv2.getId(), new BigDecimal("200.00"), "Cash");

            Reservation res3 = createReservation(elena, r101, today.minusDays(5), today.minusDays(3));
            res3.checkIn();
            res3.checkOut();
            reservations.save(res3);
            issueInvoice(res3, List.of(
                    new InvoiceLineData("Room 101 · 2 night(s)", new BigDecimal("120.00"))
            ));

            r203.changeHousekeeping(HousekeepingStatus.DIRTY);
            rooms.save(r203);
            r101.changeHousekeeping(HousekeepingStatus.INSPECTED);
            rooms.save(r101);

            Reservation res4 = createReservation(hans, r301, today.minusDays(2), today.plusDays(3));
            res4.checkIn();
            reservations.save(res4);
            charges.save(new Charge(res4.getId(), "Breakfast (2 days)", new BigDecimal("20.00")));
            charges.save(new Charge(res4.getId(), "Minibar", new BigDecimal("12.00")));
            charges.save(new Charge(res4.getId(), "Laundry Service", new BigDecimal("15.00")));

            Reservation res5 = createReservation(sofia, r201, today.minusDays(3), today);
            res5.checkIn();
            reservations.save(res5);
            charges.save(new Charge(res5.getId(), "Breakfast (3 days)", new BigDecimal("30.00")));

            Reservation res6 = createReservation(pierre, r302, today, today.plusDays(4));
            reservations.save(res6);

            Reservation res7 = createReservation(dimitris, r104, today.plusDays(3), today.plusDays(7));
            reservations.save(res7);

            Reservation res8 = createReservation(anna, r102, today.plusDays(1), today.plusDays(4));
            reservations.save(res8);

            Reservation res9 = createReservation(maria, r202, today.plusDays(5), today.plusDays(8));
            res9.cancel();
            reservations.save(res9);

            Reservation res10 = createReservation(dimitris, r104, today.minusDays(25), today.minusDays(20));
            res10.checkIn();
            res10.checkOut();
            reservations.save(res10);
            charges.save(new Charge(res10.getId(), "Breakfast (5 days)", new BigDecimal("50.00")));
            charges.save(new Charge(res10.getId(), "Parking (5 days)", new BigDecimal("25.00")));
            Invoice inv4 = issueInvoice(res10, List.of(
                    new InvoiceLineData("Room 104 · 5 night(s)", new BigDecimal("450.00")),
                    new InvoiceLineData("Breakfast (5 days)", new BigDecimal("50.00")),
                    new InvoiceLineData("Parking (5 days)", new BigDecimal("25.00"))
            ));
            billing.recordPayment(inv4.getId(), new BigDecimal("525.00"), "Transfer");
        }
    }

    private Guest saveGuest(String firstName, String lastName, String email, String phone, String documentId) {
        Guest g = new Guest();
        g.setFirstName(firstName);
        g.setLastName(lastName);
        g.setEmail(email);
        g.setPhone(phone);
        if (documentId != null) {
            g.setDocumentId(documentId);
        }
        return guests.save(g);
    }

    private Reservation createReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal price = room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
        return reservations.save(new Reservation(guest, room, checkIn, checkOut, price));
    }

    private Invoice issueInvoice(Reservation res, List<InvoiceLineData> lines) {
        return billing.issueForReservation(
                res.getId(), res.getGuest().getId(), res.getGuest().fullName(),
                res.getRoom().getNumber(), (int) res.nights(), lines
        );
    }
}
