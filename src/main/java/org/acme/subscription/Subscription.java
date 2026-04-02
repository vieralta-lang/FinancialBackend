package org.acme.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "subscriptions")
public class Subscription extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String name;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SubscriptionFrequency frequency;

    @NotBlank
    @Column(nullable = false)
    public String category;

    public LocalDate nextBillingDate;

    @Column(nullable = false)
    public boolean active = true;

    public String notes;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
