package com.vms.machine.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 36)
    private Long id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    private Instant createdAt;

    public Product(String name,
                   String description,
                   String category,
                   String imageUrl,
                   BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.basePrice = basePrice;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
