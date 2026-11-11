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

@Controller
@RequestMapping("/housekeeping")
public class HousekeepingController {

    private final HousekeepingService housekeeping;

    public HousekeepingController(HousekeepingService housekeeping) {
        this.housekeeping = housekeeping;
    }

    @GetMapping
    public String board(Model model) {
        model.addAttribute("rooms", housekeeping.board());
        return "housekeeping/board";
    }

    @PostMapping("/{roomId}/clean")
    public String clean(@PathVariable Long roomId) {
        housekeeping.setStatus(roomId, HousekeepingStatus.CLEAN);
        return "redirect:/housekeeping";
    }

    @PostMapping("/{roomId}/inspect")
    public String inspect(@PathVariable Long roomId, RedirectAttributes redirect) {
        try {
            housekeeping.setStatus(roomId, HousekeepingStatus.INSPECTED);
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping";
    }

    @PostMapping("/{roomId}/dirty")
    public String dirty(@PathVariable Long roomId) {
        housekeeping.setStatus(roomId, HousekeepingStatus.DIRTY);
        return "redirect:/housekeeping";
    }
}
