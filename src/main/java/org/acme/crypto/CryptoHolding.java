package org.acme.crypto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "crypto_holdings")
public class CryptoHolding extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String coinId = "bitcoin";

    @NotBlank
    @Column(nullable = false)
    public String symbol = "BTC";

    @NotNull
    @Column(nullable = false, precision = 20, scale = 8)
    public BigDecimal quantity;

    @Column(precision = 15, scale = 2)
    public BigDecimal averagePurchasePrice;

    public String purchaseCurrency = "USD";

    public String notes;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
