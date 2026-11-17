package gr.ihu.pms.billing;

/** Κατάσταση πληρωμής τιμολογίου: ανεξόφλητο, μερικώς εξοφλημένο, εξοφλημένο. */
public enum InvoiceStatus {
    UNPAID,
    PARTIAL,
    PAID
}
