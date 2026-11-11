package gr.ihu.pms.reservations;

import gr.ihu.pms.billing.InvoiceRepository;
import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.folio.FolioService;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.guests.GuestRepository;
import gr.ihu.pms.rooms.HousekeepingStatus;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomRepository;
import gr.ihu.pms.rooms.RoomType;
import gr.ihu.pms.rooms.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired ReservationService service;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;
    @Autowired GuestRepository guests;
    @Autowired InvoiceRepository invoices;
    @Autowired FolioService folio;

    Long guestId, roomId;
    final LocalDate in = LocalDate.now().plusDays(1);
    final LocalDate out = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        RoomType type = roomTypes.save(new RoomType("TestSuite", new BigDecimal("100.00"), 2, "t"));
        roomId = rooms.save(new Room("T-1", type, 9)).getId();
        Guest g = new Guest();
        g.setFirstName("Ada");
        g.setLastName("Lovelace");
        guestId = guests.save(g).getId();
    }

    @Test
    void bookComputesPriceFromNights() {
        Reservation r = service.book(guestId, roomId, in, out);
        assertThat(r.nights()).isEqualTo(2);
        assertThat(r.getTotalPrice()).isEqualByComparingTo("200.00");
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.BOOKED);
    }

    @Test
    void doubleBookingSameRoomIsRejected() {
        service.book(guestId, roomId, in, out);
        assertThatThrownBy(() -> service.book(guestId, roomId, in, out))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void checkoutIssuesInvoiceIncludingFolioCharges() {
        Reservation r = service.book(guestId, roomId, in, out);
        service.checkIn(r.getId());
        folio.addCharge(r.getId(), "Breakfast", new BigDecimal("30.00"));
        service.checkOut(r.getId());

        var invoice = invoices.findByReservationId(r.getId()).orElseThrow();
        assertThat(invoice.getAmount()).isEqualByComparingTo("230.00");
        assertThat(invoice.getLines()).hasSize(2);
    }

    @Test
    void checkoutMarksRoomDirty() {
        Reservation r = service.book(guestId, roomId, in, out);
        service.checkIn(r.getId());
        service.checkOut(r.getId());
        assertThat(rooms.findById(roomId).orElseThrow().getHousekeeping())
                .isEqualTo(HousekeepingStatus.DIRTY);
    }

    @Test
    void rescheduleRecomputesPrice() {
        Reservation r = service.book(guestId, roomId, in, out);
        service.reschedule(r.getId(), roomId, in, in.plusDays(3));
        assertThat(r.getTotalPrice()).isEqualByComparingTo("300.00");
    }

    @Test
    void pastCheckInIsRejected() {
        assertThatThrownBy(() -> service.book(guestId, roomId, LocalDate.now().minusDays(1), out))
                .isInstanceOf(BusinessRuleException.class);
    }
}
