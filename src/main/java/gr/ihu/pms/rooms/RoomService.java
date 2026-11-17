package gr.ihu.pms.rooms;

import gr.ihu.pms.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Λογική δωματίων. Η μόνη είσοδος των άλλων modules προς το rooms. */
@Service
public class RoomService {

    private final RoomRepository rooms;
    private final RoomTypeRepository roomTypes;

    public RoomService(RoomRepository rooms, RoomTypeRepository roomTypes) {
        this.rooms = rooms;
        this.roomTypes = roomTypes;
    }

    /** Βρίσκει δωμάτιο ή πετά NotFoundException (σελίδα 404). */
    public Room get(Long roomId) {
        return rooms.findById(roomId)
                .orElseThrow(() -> new NotFoundException("room", roomId));
    }

    /** Κλειδώνει το δωμάτιο για κράτηση. MANDATORY: δουλεύει μόνο μέσα σε ανοιχτή συναλλαγή. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Room lockForBooking(Long roomId) {
        Room room = rooms.findByIdForUpdate(roomId)
                .orElseThrow(() -> new NotFoundException("room", roomId));
        // Και η νέα κράτηση και η αλλαγή περνούν από εδώ: δωμάτιο εκτός λειτουργίας απορρίπτεται
        room.ensureInService();
        return room;
    }

    /** Όλα τα δωμάτια. */
    public List<Room> all() {
        return rooms.findAllByOrderByNumberAsc();
    }

    /** Τα δωμάτια ενός τύπου. */
    public List<Room> ofType(Long roomTypeId) {
        return rooms.findByRoomTypeIdOrderByNumberAsc(roomTypeId);
    }

    /** Όλοι οι τύποι δωματίων. */
    public List<RoomType> types() {
        return roomTypes.findAll();
    }

    /** Συνολικό πλήθος δωματίων. */
    public long count() {
        return rooms.count();
    }

    /** Πλήθος δωματίων σε λειτουργία. */
    public long countInService() {
        return rooms.countByStatus(RoomStatus.AVAILABLE);
    }

    /** Πλήθος δωματίων σε μια κατάσταση καθαριότητας. */
    public long countByHousekeeping(HousekeepingStatus status) {
        return rooms.countByHousekeeping(status);
    }

    /** Αλλάζει την κατάσταση καθαριότητας (ο κανόνας είναι μέσα στο Room). */
    @Transactional
    public void setHousekeeping(Long roomId, HousekeepingStatus status) {
        get(roomId).changeHousekeeping(status);
    }
}
