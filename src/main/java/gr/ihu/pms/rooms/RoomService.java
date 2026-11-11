package gr.ihu.pms.rooms;

import gr.ihu.pms.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository rooms;
    private final RoomTypeRepository roomTypes;

    public RoomService(RoomRepository rooms, RoomTypeRepository roomTypes) {
        this.rooms = rooms;
        this.roomTypes = roomTypes;
    }

    public Room get(Long roomId) {
        return rooms.findById(roomId)
                .orElseThrow(() -> new NotFoundException("room", roomId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Room lockForBooking(Long roomId) {
        return rooms.findByIdForUpdate(roomId)
                .orElseThrow(() -> new NotFoundException("room", roomId));
    }

    public List<Room> all() {
        return rooms.findAllByOrderByNumberAsc();
    }

    public List<Room> ofType(Long roomTypeId) {
        return rooms.findByRoomTypeIdOrderByNumberAsc(roomTypeId);
    }

    public List<RoomType> types() {
        return roomTypes.findAll();
    }

    public long count() {
        return rooms.count();
    }

    public long countInService() {
        return rooms.countByStatus(RoomStatus.AVAILABLE);
    }

    public long countByHousekeeping(HousekeepingStatus status) {
        return rooms.countByHousekeeping(status);
    }

    @Transactional
    public void setHousekeeping(Long roomId, HousekeepingStatus status) {
        get(roomId).changeHousekeeping(status);
    }
}
