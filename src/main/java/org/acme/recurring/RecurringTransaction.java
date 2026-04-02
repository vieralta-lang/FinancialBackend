package org.acme.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.acme.account.Account;
import org.acme.category.Category;
import org.acme.transaction.TransactionType;

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
@Table(name = "recurring_transactions")
public class RecurringTransaction extends PanacheEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    public Category category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransactionType type;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    public String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Frequency frequency;

    @NotNull
    @Column(nullable = false)
    public LocalDate nextDueDate;

    public LocalDate endDate;

    @Column(nullable = false)
    public boolean active = true;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
