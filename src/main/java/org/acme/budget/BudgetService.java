package org.acme.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.category.CategoryRepository;
import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionRepository;
import org.acme.transaction.TransactionType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class BudgetService {

    @Inject
    BudgetRepository budgetRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    TransactionRepository transactionRepository;

    public List<Budget> listAll() {
        return budgetRepository.listAll();
    }

    public Budget findById(Long id) {
        Budget budget = budgetRepository.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        return budget;
    }

    @Transactional
    public Budget create(Budget budget) {
        if (budget.category != null && budget.category.id != null) {
            budget.category = categoryRepository.findById(budget.category.id);
        }
        budgetRepository.persist(budget);
        return budget;
    }

    @Transactional
    public Budget update(Long id, Budget updated) {
        Budget budget = budgetRepository.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        budget.limitAmount = updated.limitAmount;
        budget.period = updated.period;
        budget.startDate = updated.startDate;
        if (updated.category != null && updated.category.id != null) {
            budget.category = categoryRepository.findById(updated.category.id);
        }
        budget.updatedAt = LocalDateTime.now();
        return budget;
    }

    @Transactional
    public void delete(Long id) {
        Budget budget = budgetRepository.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        budgetRepository.delete(budget);
    }

    public BudgetStatus getStatus(Long id) {
        Budget budget = budgetRepository.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }

        LocalDate periodStart = budget.startDate;
        LocalDate periodEnd = calculatePeriodEnd(periodStart, budget.period);

        LocalDate today = LocalDate.now();
        while (periodEnd.isBefore(today)) {
            periodStart = periodEnd.plusDays(1);
            periodEnd = calculatePeriodEnd(periodStart, budget.period);
        }

        List<Transaction> expenses = transactionRepository.listByCategoryAndTypeAndDateRange(
                budget.category.id, TransactionType.EXPENSE, periodStart, periodEnd);

        BigDecimal spent = expenses.stream()
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.limitAmount.subtract(spent);

        return new BudgetStatus(budget.id, budget.category.name,
                budget.limitAmount, spent, remaining,
                periodStart, periodEnd);
    }

    private LocalDate calculatePeriodEnd(LocalDate start, BudgetPeriod period) {
        return switch (period) {
            case WEEKLY -> start.plusWeeks(1).minusDays(1);
            case MONTHLY -> start.plusMonths(1).minusDays(1);
            case YEARLY -> start.plusYears(1).minusDays(1);
        };
    }

    public record BudgetStatus(
            Long budgetId,
            String categoryName,
            BigDecimal limitAmount,
            BigDecimal spent,
            BigDecimal remaining,
            LocalDate periodStart,
            LocalDate periodEnd) {
    }
}
