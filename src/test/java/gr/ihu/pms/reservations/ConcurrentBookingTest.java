package gr.ihu.pms.reservations;

import gr.ihu.pms.Concurrently;
import gr.ihu.pms.guests.Guest;
import gr.ihu.pms.guests.GuestRepository;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomRepository;
import gr.ihu.pms.rooms.RoomType;
import gr.ihu.pms.rooms.RoomTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrentBookingTest {

    @Autowired ReservationService service;
    @Autowired ReservationRepository reservations;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;
    @Autowired GuestRepository guests;

    RoomType type;
    Room room;
    Guest guest;

    @BeforeEach
    void setUp() {
        type = roomTypes.save(new RoomType("ConcurrencyType", new BigDecimal("80.00"), 2, "c"));
        room = rooms.save(new Room("C-101", type, 1));
        Guest g = new Guest();
        g.setFirstName("Grace");
        g.setLastName("Hopper");
        guest = guests.save(g);
    }

    @AfterEach
    void tearDown() {
        reservations.deleteAll(reservationsOfRoom());
        rooms.delete(room);
        roomTypes.delete(type);
        guests.delete(guest);
    }

    @Test
    void concurrentBookingsOfTheSameRoomLetExactlyOneThrough() throws Exception {
        LocalDate in = LocalDate.now().plusDays(30);
        LocalDate out = in.plusDays(2);

        Concurrently.Result result = Concurrently.run(5,
                () -> service.book(guest.getId(), room.getId(), in, out));

        assertThat(result.successes()).as("bookings that went through").isEqualTo(1);
        assertThat(reservationsOfRoom()).as("reservations stored for the room").hasSize(1);
    }

    private List<Reservation> reservationsOfRoom() {
        return reservations.findAll().stream()
                .filter(r -> r.getRoom().getId().equals(room.getId()))
                .toList();
    }
}
