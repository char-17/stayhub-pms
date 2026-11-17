package gr.ihu.pms.rooms;

import gr.ihu.pms.common.BusinessRuleException;
import jakarta.persistence.*;

/** Δωμάτιο: αριθμός, τύπος, όροφος, αν είναι σε λειτουργία και κατάσταση καθαριότητας. */
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private RoomType roomType;

    @Column(nullable = false)
    private int floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HousekeepingStatus housekeeping = HousekeepingStatus.CLEAN;

    protected Room() {
    }

    public Room(String number, RoomType roomType, int floor) {
        this.number = number;
        this.roomType = roomType;
        this.floor = floor;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public int getFloor() {
        return floor;
    }

    public RoomStatus getStatus() {
        return status;
    }

    /** Θέτει το δωμάτιο εκτός λειτουργίας. */
    public void takeOutOfService() {
        this.status = RoomStatus.OUT_OF_SERVICE;
    }

    /**
     * Ελέγχει ότι το δωμάτιο είναι σε λειτουργία και μπορεί να δοθεί σε κράτηση.
     * Πριν, ο έλεγχος υπήρχε μόνο στη λίστα διαθέσιμων δωματίων, οπότε μια αλλαγή κράτησης
     * μπορούσε να μεταφέρει την κράτηση σε δωμάτιο εκτός λειτουργίας.
     */
    public void ensureInService() {
        if (status != RoomStatus.AVAILABLE) {
            throw new BusinessRuleException("Room " + number + " is out of service");
        }
    }

    public HousekeepingStatus getHousekeeping() {
        return housekeeping;
    }

    /** Αλλάζει την κατάσταση καθαριότητας· INSPECTED μόνο από CLEAN. */
    public void changeHousekeeping(HousekeepingStatus target) {
        if (target == HousekeepingStatus.INSPECTED && housekeeping != HousekeepingStatus.CLEAN) {
            throw new BusinessRuleException("Only a clean room can be marked as inspected");
        }
        this.housekeeping = target;
    }
}
