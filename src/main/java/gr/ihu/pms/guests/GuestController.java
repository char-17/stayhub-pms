package gr.ihu.pms.guests;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/** Σελίδες πελατών (/guests): λίστα με αναζήτηση και φόρμα νέου πελάτη. */
@Controller
@RequestMapping("/guests")
public class GuestController {

    private final GuestService guests;

    public GuestController(GuestService guests) {
        this.guests = guests;
    }

    /** Λίστα πελατών, με προαιρετική αναζήτηση στο επώνυμο. */
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("guests", guests.search(q));
        model.addAttribute("q", q);
        return "guests/list";
    }

    /** Άδεια φόρμα νέου πελάτη. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("guest", new Guest());
        return "guests/form";
    }

    /** Αποθηκεύει νέο πελάτη· με λάθη επικύρωσης ξαναδείχνει τη φόρμα. */
    @PostMapping
    public String create(@Valid @ModelAttribute("guest") Guest guest, BindingResult binding) {
        if (binding.hasErrors()) {
            return "guests/form";
        }
        guests.create(guest);
        return "redirect:/guests";
    }
}
