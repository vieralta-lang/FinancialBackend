package org.acme.recurring;

import java.time.LocalDate;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecurringTransactionRepository implements PanacheRepository<RecurringTransaction> {

    public List<RecurringTransaction> findActiveDueBefore(LocalDate date) {
        return list("active = true and nextDueDate <= ?1", date);
    }
}
