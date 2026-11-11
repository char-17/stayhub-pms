package gr.ihu.pms.housekeeping;

import gr.ihu.pms.rooms.HousekeepingStatus;
import gr.ihu.pms.rooms.Room;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HousekeepingService {

    private final RoomService rooms;

    public HousekeepingService(RoomService rooms) {
        this.rooms = rooms;
    }

    public List<Room> board() {
        return rooms.all();
    }

    @Transactional
    public void setStatus(Long roomId, HousekeepingStatus status) {
        rooms.setHousekeeping(roomId, status);
    }

    @Transactional
    public void roomVacated(Long roomId) {
        rooms.setHousekeeping(roomId, HousekeepingStatus.DIRTY);
    }
}
