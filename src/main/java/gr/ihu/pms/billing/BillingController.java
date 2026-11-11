package gr.ihu.pms.billing;

import gr.ihu.pms.common.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billing;

    public BillingController(BillingService billing) {
        this.billing = billing;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rows", billing.rows());
        return "billing/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invoice invoice = billing.get(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments", billing.paymentsFor(id));
        model.addAttribute("balance", billing.balanceOf(invoice));
        return "billing/detail";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam BigDecimal amount,
                      @RequestParam(required = false) String method, RedirectAttributes redirect) {
        try {
            billing.recordPayment(id, amount, method);
            redirect.addFlashAttribute("message", "Payment recorded.");
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billing/" + id;
    }
}
