package org.acme.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.acme.account.Account;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "debts")
public class Debt extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String person;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DebtDirection direction;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    public String description;

    public LocalDate dueDate;

    @Column(nullable = false)
    public boolean paid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    public Account paidWithAccount;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
