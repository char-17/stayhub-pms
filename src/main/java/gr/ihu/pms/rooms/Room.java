package gr.ihu.pms.rooms;

import gr.ihu.pms.common.BusinessRuleException;
import jakarta.persistence.*;

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

    public void takeOutOfService() {
        this.status = RoomStatus.OUT_OF_SERVICE;
    }

    public HousekeepingStatus getHousekeeping() {
        return housekeeping;
    }

    public void changeHousekeeping(HousekeepingStatus target) {
        if (target == HousekeepingStatus.INSPECTED && housekeeping != HousekeepingStatus.CLEAN) {
            throw new BusinessRuleException("Only a clean room can be marked as inspected");
        }
        this.housekeeping = target;
    }
}
