package gr.ihu.pms.guests;

import gr.ihu.pms.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Λογική πελατών. Η μόνη είσοδος των άλλων modules προς το guests. */
@Service
public class GuestService {

    private final GuestRepository guests;

    public GuestService(GuestRepository guests) {
        this.guests = guests;
    }

    /** Βρίσκει πελάτη ή πετά NotFoundException (σελίδα 404). */
    public Guest get(Long guestId) {
        return guests.findById(guestId)
                .orElseThrow(() -> new NotFoundException("guest", guestId));
    }

    /** Όλοι οι πελάτες, αλφαβητικά. */
    public List<Guest> all() {
        return guests.findAllByOrderByLastNameAscFirstNameAsc();
    }

    /** Αναζήτηση στο επώνυμο· κενό κριτήριο επιστρέφει όλους. */
    public List<Guest> search(String lastName) {
        return (lastName == null || lastName.isBlank())
                ? all()
                : guests.findByLastNameContainingIgnoreCaseOrderByLastNameAsc(lastName);
    }

    /** Αποθηκεύει νέο πελάτη. */
    @Transactional
    public Guest create(Guest guest) {
        return guests.save(guest);
    }
}
