package gr.ihu.pms.housekeeping;

import gr.ihu.pms.rooms.HousekeepingStatus;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Λογική καθαριότητας. Αλλάζει την κατάσταση των δωματίων μέσω του RoomService. */
@Service
public class HousekeepingService {

    private final RoomService rooms;

    public HousekeepingService(RoomService rooms) {
        this.rooms = rooms;
    }

    /** Τα δωμάτια για τον πίνακα καθαριότητας. */
    public List<Room> board() {
        return rooms.all();
    }

    /** Αλλάζει την κατάσταση καθαριότητας ενός δωματίου. */
    @Transactional
    public void setStatus(Long roomId, HousekeepingStatus status) {
        rooms.setHousekeeping(roomId, status);
    }

    /** Το δωμάτιο άδειασε (check-out) → γίνεται DIRTY. */
    @Transactional
    public void roomVacated(Long roomId) {
        rooms.setHousekeeping(roomId, HousekeepingStatus.DIRTY);
    }
}
