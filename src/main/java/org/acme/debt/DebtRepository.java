package org.acme.debt;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DebtRepository implements PanacheRepository<Debt> {
}
