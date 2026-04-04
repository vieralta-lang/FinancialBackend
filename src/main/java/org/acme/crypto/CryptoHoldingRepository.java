package org.acme.crypto;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CryptoHoldingRepository implements PanacheRepository<CryptoHolding> {
}
