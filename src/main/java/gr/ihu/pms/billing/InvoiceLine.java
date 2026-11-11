package gr.ihu.pms.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Invoice invoice;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    protected InvoiceLine() {
    }

    public InvoiceLine(Invoice invoice, String description, BigDecimal amount) {
        this.invoice = invoice;
        this.description = description;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
