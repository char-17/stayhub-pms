package gr.ihu.pms.reports;

import gr.ihu.pms.common.BusinessRuleException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/reports")
    public String report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        LocalDate f = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate t = to != null ? to : f.plusMonths(1);
        model.addAttribute("from", f);
        model.addAttribute("to", t);
        try {
            model.addAttribute("report", reports.between(f, t));
        } catch (BusinessRuleException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reports/report";
    }
}
