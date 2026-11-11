package gr.ihu.pms.web;

import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.reservations.ReservationService;
import gr.ihu.pms.rooms.HousekeepingStatus;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final RoomService rooms;
    private final ReservationService reservations;
    private final BillingService billing;

    public DashboardController(RoomService rooms, ReservationService reservations,
                              BillingService billing) {
        this.rooms = rooms;
        this.reservations = reservations;
        this.billing = billing;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        long total = rooms.count();
        long inHouse = reservations.countInHouse();
        model.addAttribute("totalRooms", total);
        model.addAttribute("roomsInService", rooms.countInService());
        model.addAttribute("inHouse", inHouse);
        model.addAttribute("occupancy", total == 0 ? 0 : Math.round(100.0 * inHouse / total));
        model.addAttribute("arrivals", reservations.arrivalsOn(today));
        model.addAttribute("departures", reservations.departuresOn(today));
        model.addAttribute("unpaid", billing.countUnpaid());
        model.addAttribute("toClean", rooms.countByHousekeeping(HousekeepingStatus.DIRTY));
        return "dashboard";
    }
}
