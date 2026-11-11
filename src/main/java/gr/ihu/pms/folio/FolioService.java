package gr.ihu.pms.folio;

import gr.ihu.pms.common.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FolioService {

    private final ChargeRepository charges;

    public FolioService(ChargeRepository charges) {
        this.charges = charges;
    }

    @Transactional
    public Charge addCharge(Long reservationId, String description, BigDecimal amount) {
        if (description == null || description.isBlank()) {
            throw new BusinessRuleException("Description is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Amount must be positive");
        }
        return charges.save(new Charge(reservationId, description, amount));
    }

    public List<Charge> forReservation(Long reservationId) {
        return charges.findByReservationIdOrderByCreatedAtAsc(reservationId);
    }

    public BigDecimal total(Long reservationId) {
        return forReservation(reservationId).stream()
                .map(Charge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
