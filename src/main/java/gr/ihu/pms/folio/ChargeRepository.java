package gr.ihu.pms.folio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Πρόσβαση στη βάση για τις χρεώσεις. */
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    /** Οι χρεώσεις μιας κράτησης, με χρονολογική σειρά. */
    List<Charge> findByReservationIdOrderByCreatedAtAsc(Long reservationId);
}
