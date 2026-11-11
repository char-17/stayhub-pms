package gr.ihu.pms.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    private final boolean demoData;

    public AuthViewController(@Value("${pms.demo-data:false}") boolean demoData) {
        this.demoData = demoData;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("demoData", demoData);
        return "login";
    }
}
