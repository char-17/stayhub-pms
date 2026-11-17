package gr.ihu.pms.guests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Πρόσβαση στη βάση για τους πελάτες. */
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /** Αναζήτηση στο επώνυμο, χωρίς διάκριση πεζών-κεφαλαίων. */
    List<Guest> findByLastNameContainingIgnoreCaseOrderByLastNameAsc(String lastName);

    /** Όλοι οι πελάτες, αλφαβητικά. */
    List<Guest> findAllByOrderByLastNameAscFirstNameAsc();
}
