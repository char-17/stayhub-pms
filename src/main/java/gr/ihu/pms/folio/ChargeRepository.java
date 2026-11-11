package gr.ihu.pms.folio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChargeRepository extends JpaRepository<Charge, Long> {

    List<Charge> findByReservationIdOrderByCreatedAtAsc(Long reservationId);
}
