package org.acme.recurring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionService;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RecurringTransactionScheduler {

    @Inject
    TransactionService transactionService;

    @Inject
    RecurringTransactionRepository recurringTransactionRepository;

    /**
     * Runs daily at midnight to generate transactions from recurring templates.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processRecurring() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringTransactionRepository.findActiveDueBefore(today);

        for (RecurringTransaction rt : due) {
            // Check if end date has passed
            if (rt.endDate != null && today.isAfter(rt.endDate)) {
                rt.active = false;
                rt.updatedAt = LocalDateTime.now();
                continue;
            }

            // Create the actual transaction
            Transaction tx = new Transaction();
            tx.account = rt.account;
            tx.category = rt.category;
            tx.type = rt.type;
            tx.amount = rt.amount;
            tx.description = rt.description;
            tx.date = rt.nextDueDate;
            transactionService.createTransaction(tx);

            // Advance to next due date
            rt.nextDueDate = calculateNextDate(rt.nextDueDate, rt.frequency);
            rt.updatedAt = LocalDateTime.now();
        }
    }

    private LocalDate calculateNextDate(LocalDate current, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
