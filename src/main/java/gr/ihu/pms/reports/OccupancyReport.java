package gr.ihu.pms.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OccupancyReport(
        LocalDate from,
        LocalDate to,
        long nights,
        long rooms,
        long availableRoomNights,
        long soldRoomNights,
        long occupancyPct,
        BigDecimal revenue,
        BigDecimal collected,
        BigDecimal adr,
        BigDecimal revpar,
        long arrivals,
        long departures) {
}
