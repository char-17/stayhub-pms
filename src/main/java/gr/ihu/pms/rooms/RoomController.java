package gr.ihu.pms.rooms;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {

    private final RoomService rooms;

    public RoomController(RoomService rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/rooms")
    public String list(Model model) {
        model.addAttribute("rooms", rooms.all());
        model.addAttribute("roomTypes", rooms.types());
        return "rooms/list";
    }
}
