package gr.ihu.pms.reservations;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.folio.FolioService;
import gr.ihu.pms.guests.GuestService;
import gr.ihu.pms.rooms.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;
    private final ReservationRepository reservations;
    private final GuestService guests;
    private final RoomService rooms;
    private final FolioService folio;

    public ReservationController(ReservationService service, ReservationRepository reservations,
                                 GuestService guests, RoomService rooms, FolioService folio) {
        this.service = service;
        this.reservations = reservations;
        this.guests = guests;
        this.rooms = rooms;
        this.folio = folio;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status, Model model) {
        model.addAttribute("reservations", status == null
                ? reservations.findAllByOrderByCheckInDesc()
                : reservations.findByStatusOrderByCheckInDesc(status));
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("status", status);
        return "reservations/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Reservation r = get(id);
        BigDecimal charges = folio.total(id);
        model.addAttribute("reservation", r);
        model.addAttribute("charges", folio.forReservation(id));
        model.addAttribute("chargesTotal", charges);
        model.addAttribute("grandTotal", r.getTotalPrice().add(charges));
        return "reservations/detail";
    }

    @PostMapping("/{id}/charges")
    public String addCharge(@PathVariable Long id, @RequestParam String description,
                            @RequestParam BigDecimal amount, RedirectAttributes redirect) {
        Reservation r = get(id);
        if (r.getStatus() != ReservationStatus.BOOKED && r.getStatus() != ReservationStatus.CHECKED_IN) {
            redirect.addFlashAttribute("error", "Charges can only be posted to an active stay.");
            return "redirect:/reservations/" + id;
        }
        try {
            folio.addCharge(id, description, amount);
            redirect.addFlashAttribute("message", "Charge posted.");
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Reservation r = get(id);
        model.addAttribute("reservation", r);
        model.addAttribute("roomsOfType", rooms.ofType(r.getRoom().getRoomType().getId()));
        return "reservations/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @RequestParam Long roomId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                       RedirectAttributes redirect) {
        try {
            service.reschedule(id, roomId, checkIn, checkOut);
            redirect.addFlashAttribute("message", "Reservation updated.");
            return "redirect:/reservations/" + id;
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations/" + id + "/edit";
        }
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long guestId,
                          @RequestParam(required = false) Long roomTypeId,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                          Model model) {
        model.addAttribute("guests", guests.all());
        model.addAttribute("roomTypes", rooms.types());
        model.addAttribute("guestId", guestId);
        model.addAttribute("roomTypeId", roomTypeId);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        if (roomTypeId != null && checkIn != null && checkOut != null) {
            try {
                model.addAttribute("available", service.availableRooms(roomTypeId, checkIn, checkOut));
                model.addAttribute("searched", true);
            } catch (BusinessRuleException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "reservations/form";
    }

    @PostMapping
    public String book(@RequestParam Long guestId, @RequestParam Long roomId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                       RedirectAttributes redirect) {
        try {
            Reservation r = service.book(guestId, roomId, checkIn, checkOut);
            redirect.addFlashAttribute("message", "Reservation created.");
            return "redirect:/reservations/" + r.getId();
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations/new";
        }
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.checkIn(id), "Checked in.");
    }

    @PostMapping("/{id}/check-out")
    public String checkOut(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.checkOut(id), "Checked out — invoice issued.");
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.cancel(id), "Reservation cancelled.");
    }

    private String act(RedirectAttributes redirect, Runnable action, String ok) {
        try {
            action.run();
            redirect.addFlashAttribute("message", ok);
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    private Reservation get(Long id) {
        return service.get(id);
    }
}
