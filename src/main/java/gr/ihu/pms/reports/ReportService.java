package gr.ihu.pms.reports;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.reservations.Reservation;
import gr.ihu.pms.reservations.ReservationService;
import gr.ihu.pms.reservations.ReservationStatus;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReportService {

    private static final List<ReservationStatus> SOLD = List.of(
            ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN, ReservationStatus.CHECKED_OUT);

    private final ReservationService reservations;
    private final RoomService rooms;
    private final BillingService billing;

    public ReportService(ReservationService reservations, RoomService rooms, BillingService billing) {
        this.reservations = reservations;
        this.rooms = rooms;
        this.billing = billing;
    }

    public OccupancyReport between(LocalDate from, LocalDate to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new BusinessRuleException("'To' must be after 'From'");
        }
        long nights = ChronoUnit.DAYS.between(from, to);
        long roomCount = rooms.count();
        long availableRoomNights = roomCount * nights;

        long soldRoomNights = 0;
        for (Reservation r : reservations.soldBetween(from, to, SOLD)) {
            LocalDate start = r.getCheckIn().isAfter(from) ? r.getCheckIn() : from;
            LocalDate end = r.getCheckOut().isBefore(to) ? r.getCheckOut() : to;
            soldRoomNights += ChronoUnit.DAYS.between(start, end);
        }

        long occupancyPct = availableRoomNights == 0 ? 0
                : Math.round(100.0 * soldRoomNights / availableRoomNights);

        BigDecimal revenue = billing.invoicedBetween(from, to);
        BigDecimal collected = billing.collectedBetween(from, to);
        BigDecimal adr = soldRoomNights == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(soldRoomNights), 2, RoundingMode.HALF_UP);
        BigDecimal revpar = availableRoomNights == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(availableRoomNights), 2, RoundingMode.HALF_UP);

        return new OccupancyReport(from, to, nights, roomCount, availableRoomNights, soldRoomNights,
                occupancyPct, revenue, collected, adr, revpar,
                reservations.countArrivals(from, to), reservations.countDepartures(from, to));
    }
}
