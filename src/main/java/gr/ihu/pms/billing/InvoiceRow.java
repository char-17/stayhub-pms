package gr.ihu.pms.billing;

import java.math.BigDecimal;

public record InvoiceRow(Invoice invoice, BigDecimal paid, BigDecimal balance) {
}
