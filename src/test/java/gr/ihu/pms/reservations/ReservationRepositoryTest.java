package gr.ihu.pms.reservations;

import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.guests.GuestRepository;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomRepository;
import gr.ihu.pms.rooms.RoomType;
import gr.ihu.pms.rooms.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static gr.ihu.pms.reservations.ReservationService.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired ReservationRepository reservations;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;
    @Autowired GuestRepository guests;

    RoomType type;
    Room r1, r2;
    Guest guest;
    final LocalDate d1 = LocalDate.of(2026, 6, 1);
    final LocalDate d3 = LocalDate.of(2026, 6, 3);
    final LocalDate d5 = LocalDate.of(2026, 6, 5);

    @BeforeEach
    void setUp() {
        type = roomTypes.save(new RoomType("Double", new BigDecimal("90.00"), 2, "d"));
        r1 = rooms.save(new Room("101", type, 1));
        r2 = rooms.save(new Room("102", type, 1));
        guest = new Guest();
        guest.setFirstName("Test");
        guest.setLastName("Guest");
        guest = guests.save(guest);
    }

    @Test
    void allRoomsAvailableWhenNoReservations() {
        List<Room> free = reservations.findAvailableRooms(type.getId(), d1, d3, ACTIVE);
        assertThat(free).extracting(Room::getNumber).containsExactly("101", "102");
    }

    @Test
    void bookedRoomIsExcludedForOverlappingDates() {
        reservations.save(new Reservation(guest, r1, d1, d3, new BigDecimal("180.00")));

        List<Room> free = reservations.findAvailableRooms(type.getId(), d1, d3, ACTIVE);
        assertThat(free).extracting(Room::getNumber).containsExactly("102");
        assertThat(reservations.hasOverlap(r1.getId(), d1, d3, ACTIVE)).isTrue();
        assertThat(reservations.hasOverlap(r2.getId(), d1, d3, ACTIVE)).isFalse();
    }

    @Test
    void adjacentStayDoesNotOverlap() {
        reservations.save(new Reservation(guest, r1, d1, d3, new BigDecimal("180.00")));

        assertThat(reservations.hasOverlap(r1.getId(), d3, d5, ACTIVE)).isFalse();
        assertThat(reservations.findAvailableRooms(type.getId(), d3, d5, ACTIVE))
                .extracting(Room::getNumber).contains("101");
    }

    @Test
    void cancelledReservationDoesNotBlock() {
        Reservation res = new Reservation(guest, r1, d1, d3, new BigDecimal("180.00"));
        res.cancel();
        reservations.save(res);

        assertThat(reservations.hasOverlap(r1.getId(), d1, d3, ACTIVE)).isFalse();
        assertThat(reservations.findAvailableRooms(type.getId(), d1, d3, ACTIVE)).hasSize(2);
    }
}
