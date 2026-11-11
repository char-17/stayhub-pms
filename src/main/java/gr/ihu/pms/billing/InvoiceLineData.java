package gr.ihu.pms.billing;

import java.math.BigDecimal;

public record InvoiceLineData(String description, BigDecimal amount) {
}
