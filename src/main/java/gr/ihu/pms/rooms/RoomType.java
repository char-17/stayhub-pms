package gr.ihu.pms.rooms;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private int capacity;

    @Column(length = 500)
    private String description;

    protected RoomType() {
    }

    public RoomType(String name, BigDecimal basePrice, int capacity, String description) {
        this.name = name;
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }
}
