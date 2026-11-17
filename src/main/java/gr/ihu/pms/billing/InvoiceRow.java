package gr.ihu.pms.billing;

import java.math.BigDecimal;


//Απλά record, δεν αποθηκεύονται
public record InvoiceRow(Invoice invoice, BigDecimal paid, BigDecimal balance) {
}
