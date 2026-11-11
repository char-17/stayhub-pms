package gr.ihu.pms.reservations;

import gr.ihu.pms.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private final LocalDate in = LocalDate.of(2030, 1, 10);
    private final LocalDate out = LocalDate.of(2030, 1, 12);

    private Reservation booked() {
        return new Reservation(null, null, in, out, new BigDecimal("200.00"));
    }

    @Test
    void normalLifecycleIsBookedThenCheckedInThenCheckedOut() {
        Reservation r = booked();
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        r.checkIn();
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        r.checkOut();
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CHECKED_OUT);
    }

    @Test
    void checkOutWithoutCheckInIsRejected() {
        Reservation r = booked();
        assertThatThrownBy(r::checkOut).isInstanceOf(BusinessRuleException.class);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.BOOKED);
    }

    @Test
    void cancelIsAllowedOnlyBeforeArrival() {
        Reservation r = booked();
        r.checkIn();
        assertThatThrownBy(r::cancel).isInstanceOf(BusinessRuleException.class);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
    }

    @Test
    void cancelledReservationCannotBeRevived() {
        Reservation r = booked();
        r.cancel();
        assertThatThrownBy(r::checkIn).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(r::checkOut).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(r::cancel).isInstanceOf(BusinessRuleException.class);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void rescheduleIsRejectedAfterArrival() {
        Reservation r = booked();
        r.checkIn();
        assertThatThrownBy(() -> r.reschedule(null, in, out.plusDays(1), new BigDecimal("300.00")))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(r.getCheckOut()).isEqualTo(out);
        assertThat(r.getTotalPrice()).isEqualByComparingTo("200.00");
    }
}
