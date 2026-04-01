package org.acme.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.acme.account.Account;
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
@Table(name = "transactions")
public class Transaction extends PanacheEntity {

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

    @NotNull
    @Column(nullable = false)
    public LocalDate date;

    public String description;

    /**
     * Links two transactions that form a transfer.
     * Both sides share the same transferId.
     */
    public Long transferId;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
