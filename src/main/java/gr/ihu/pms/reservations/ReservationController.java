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

/**
 * Σελίδες κρατήσεων (/reservations): λίστα, λεπτομέρειες, νέα κράτηση, αλλαγή,
 * check-in, check-out, ακύρωση και χρεώσεις.
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    // Ο controller μιλά μόνο με υπηρεσίες· και η λίστα έρχεται από την υπηρεσία (service.list)
    private final ReservationService service;
    private final GuestService guests;
    private final RoomService rooms;
    private final FolioService folio;

    public ReservationController(ReservationService service,
                                 GuestService guests, RoomService rooms, FolioService folio) {
        this.service = service;
        this.guests = guests;
        this.rooms = rooms;
        this.folio = folio;
    }

    /** Λίστα κρατήσεων, με προαιρετικό φίλτρο κατάστασης. */
    @GetMapping
    public String list(@RequestParam(required = false) ReservationStatus status, Model model) {
        // Η λίστα (με ή χωρίς φίλτρο) έρχεται από την υπηρεσία, όχι από το repository
        model.addAttribute("reservations", service.list(status));
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("status", status);
        return "reservations/list";
    }

    /** Μια κράτηση με τις χρεώσεις της και το γενικό σύνολο. */
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

    /** Προσθέτει χρέωση folio· μόνο σε ενεργή διαμονή. */
    @PostMapping("/{id}/charges")
    public String addCharge(@PathVariable Long id, @RequestParam String description,
                            @RequestParam BigDecimal amount, RedirectAttributes redirect) {
        // Ο κανόνας "μόνο σε ενεργή διαμονή" ελέγχεται από την ίδια την κράτηση, μέσα από τη
        // ReservationService.addCharge (που κλειδώνει και την κράτηση)· εδώ μόνο το μήνυμα
        try {
            service.addCharge(id, description, amount);
            redirect.addFlashAttribute("message", "Charge posted.");
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations/" + id;
    }

    /** Φόρμα αλλαγής κράτησης. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Reservation r = get(id);
        model.addAttribute("reservation", r);
        model.addAttribute("roomsOfType", rooms.ofType(r.getRoom().getRoomType().getId()));
        return "reservations/edit";
    }

    /** Αλλάζει δωμάτιο ή ημερομηνίες. */
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

    /** Φόρμα νέας κράτησης· με τύπο και ημερομηνίες δείχνει τα διαθέσιμα δωμάτια. */
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

    /** Δημιουργεί την κράτηση· αν το δωμάτιο δεν είναι διαθέσιμο, δείχνει το μήνυμα. */
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

    /** Check-in. */
    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.checkIn(id), "Checked in.");
    }

    /** Check-out: εκδίδεται και το τιμολόγιο. */
    @PostMapping("/{id}/check-out")
    public String checkOut(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.checkOut(id), "Checked out — invoice issued.");
    }

    /** Ακύρωση κράτησης. */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        return act(redirect, () -> service.cancel(id), "Reservation cancelled.");
    }

    /** Εκτελεί μια ενέργεια και δείχνει μήνυμα επιτυχίας ή το μήνυμα του κανόνα που παραβιάστηκε. */
    private String act(RedirectAttributes redirect, Runnable action, String ok) {
        try {
            action.run();
            redirect.addFlashAttribute("message", ok);
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    /** Βρίσκει την κράτηση ή 404. */
    private Reservation get(Long id) {
        return service.get(id);
    }
}
