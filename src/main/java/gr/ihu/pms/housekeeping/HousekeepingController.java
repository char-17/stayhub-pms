package gr.ihu.pms.housekeeping;

import gr.ihu.pms.common.BusinessRuleException;
import gr.ihu.pms.rooms.HousekeepingStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Πίνακας καθαριότητας (/housekeeping): αλλαγή κατάστασης δωματίου. */
@Controller
@RequestMapping("/housekeeping")
public class HousekeepingController {

    private final HousekeepingService housekeeping;

    public HousekeepingController(HousekeepingService housekeeping) {
        this.housekeeping = housekeeping;
    }

    /** Όλα τα δωμάτια με την κατάσταση καθαριότητάς τους. */
    @GetMapping
    public String board(Model model) {
        model.addAttribute("rooms", housekeeping.board());
        return "housekeeping/board";
    }

    /** Σημαίνει το δωμάτιο ως καθαρό. */
    @PostMapping("/{roomId}/clean")
    public String clean(@PathVariable Long roomId) {
        housekeeping.setStatus(roomId, HousekeepingStatus.CLEAN);
        return "redirect:/housekeeping";
    }

    /** Σημαίνει το δωμάτιο ως επιθεωρημένο· επιτρέπεται μόνο αν είναι καθαρό. */
    @PostMapping("/{roomId}/inspect")
    public String inspect(@PathVariable Long roomId, RedirectAttributes redirect) {
        try {
            housekeeping.setStatus(roomId, HousekeepingStatus.INSPECTED);
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping";
    }

    /** Σημαίνει το δωμάτιο ως βρώμικο. */
    @PostMapping("/{roomId}/dirty")
    public String dirty(@PathVariable Long roomId) {
        housekeeping.setStatus(roomId, HousekeepingStatus.DIRTY);
        return "redirect:/housekeeping";
    }
}
