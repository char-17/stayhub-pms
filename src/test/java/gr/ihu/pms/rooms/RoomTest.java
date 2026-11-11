package gr.ihu.pms.rooms;

import gr.ihu.pms.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTest {

    private Room room() {
        return new Room("101", null, 1);
    }

    @Test
    void housekeepingFlowIsDirtyThenCleanThenInspected() {
        Room room = room();
        room.changeHousekeeping(HousekeepingStatus.DIRTY);
        room.changeHousekeeping(HousekeepingStatus.CLEAN);
        room.changeHousekeeping(HousekeepingStatus.INSPECTED);
        assertThat(room.getHousekeeping()).isEqualTo(HousekeepingStatus.INSPECTED);
        room.changeHousekeeping(HousekeepingStatus.DIRTY);
        assertThat(room.getHousekeeping()).isEqualTo(HousekeepingStatus.DIRTY);
    }

    @Test
    void dirtyRoomCannotBeMarkedInspected() {
        Room room = room();
        room.changeHousekeeping(HousekeepingStatus.DIRTY);
        assertThatThrownBy(() -> room.changeHousekeeping(HousekeepingStatus.INSPECTED))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(room.getHousekeeping()).isEqualTo(HousekeepingStatus.DIRTY);
    }
}
