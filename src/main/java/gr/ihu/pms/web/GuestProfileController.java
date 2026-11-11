package gr.ihu.pms.web;

import gr.ihu.pms.billing.BillingService;
import gr.ihu.pms.billing.InvoiceRow;
import gr.ihu.pms.guests.GuestService;
import gr.ihu.pms.reservations.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class GuestProfileController {

    private final GuestService guests;
    private final ReservationService reservations;
    private final BillingService billing;

    public GuestProfileController(GuestService guests, ReservationService reservations,
                                  BillingService billing) {
        this.guests = guests;
        this.reservations = reservations;
        this.billing = billing;
    }

    @GetMapping("/guests/{id}")
    public String profile(@PathVariable Long id, Model model) {
        List<InvoiceRow> invoiceRows = billing.rowsForGuest(id);
        BigDecimal invoiced = invoiceRows.stream().map(r -> r.invoice().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = invoiceRows.stream().map(InvoiceRow::paid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("guest", guests.get(id));
        model.addAttribute("reservations", reservations.forGuest(id));
        model.addAttribute("invoiceRows", invoiceRows);
        model.addAttribute("invoiced", invoiced);
        model.addAttribute("paid", paid);
        return "guests/detail";
    }
}
