package gr.ihu.pms.billing;

import java.math.BigDecimal;

//Απλά record, δεν αποθηκεύονται

/**
 * Μεταφορέας δεδομένων: περιγραφή και ποσό μιας γραμμής, από τις κρατήσεις προς το billing.
 * Δεν αποθηκεύεται στη βάση.
 */
public record InvoiceLineData(String description, BigDecimal amount) {
}
