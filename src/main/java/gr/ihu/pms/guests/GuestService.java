package gr.ihu.pms.guests;

import gr.ihu.pms.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestService {

    private final GuestRepository guests;

    public GuestService(GuestRepository guests) {
        this.guests = guests;
    }

    public Guest get(Long guestId) {
        return guests.findById(guestId)
                .orElseThrow(() -> new NotFoundException("guest", guestId));
    }

    public List<Guest> all() {
        return guests.findAllByOrderByLastNameAscFirstNameAsc();
    }

    public List<Guest> search(String lastName) {
        return (lastName == null || lastName.isBlank())
                ? all()
                : guests.findByLastNameContainingIgnoreCaseOrderByLastNameAsc(lastName);
    }

    @Transactional
    public Guest create(Guest guest) {
        return guests.save(guest);
    }
}
