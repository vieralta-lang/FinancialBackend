package org.acme.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.acme.category.Category;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "budgets")
public class Budget extends PanacheEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Category category;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal limitAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BudgetPeriod period;

    @NotNull
    @Column(nullable = false)
    public LocalDate startDate;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
