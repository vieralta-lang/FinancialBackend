package org.acme.account;

import java.math.BigDecimal;
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
@Table(name = "accounts")
public class Account extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AccountType type;

    @NotBlank
    @Column(nullable = false, length = 3)
    public String currency = "BRL";

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal initialBalance = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    public boolean active = true;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
