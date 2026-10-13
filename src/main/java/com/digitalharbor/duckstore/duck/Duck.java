
package com.digitalharbor.duckstore.duck;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "ducks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ducks_active_key",
                        columnNames = "active_key"
                )
        }
)
public class Duck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DuckColor color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DuckSize size;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "active_key", length = 100)
    private String activeKey;

    public Duck() {
    }

    @PrePersist
    @PreUpdate
    private void updateActiveKey() {
        if (!deleted && color != null && size != null && price != null) {
            String normalizedPrice = price
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();

            this.activeKey = color.name() + "|"
                    + size.name() + "|"
                    + normalizedPrice;
        } else {
            this.activeKey = null;
        }
    }

    public Long getId() {
        return id;
    }

    public DuckColor getColor() {
        return color;
    }

    public void setColor(DuckColor color) {
        this.color = color;
    }

    public DuckSize getSize() {
        return size;
    }

    public void setSize(DuckSize size) {
        this.size = size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getActiveKey() {
        return activeKey;
    }
}
