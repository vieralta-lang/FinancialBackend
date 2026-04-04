package org.acme.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionRepository;
import org.acme.transaction.TransactionType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReportService {

    @Inject
    TransactionRepository transactionRepository;

    public MonthlySummary monthlySummary(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions = transactionRepository.listByDateRange(from, to);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.type == TransactionType.INCOME)
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.type == TransactionType.EXPENSE)
                .map(t -> t.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new MonthlySummary(from, to, totalIncome, totalExpense, balance);
    }

    public List<CategoryBreakdown> byCategory(LocalDate from, LocalDate to, TransactionType type) {
        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions;
        if (type != null) {
            transactions = transactionRepository.listByTypeAndDateRange(type, from, to);
        } else {
            transactions = transactionRepository.listByDateRange(from, to);
        }

        Map<String, BigDecimal> grouped = transactions.stream()
                .filter(t -> t.category != null)
                .collect(Collectors.groupingBy(
                        t -> t.category.name,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.amount, BigDecimal::add)));

        return grouped.entrySet().stream()
                .map(e -> new CategoryBreakdown(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.total.compareTo(a.total))
                .toList();
    }

    public List<DailyBalance> cashFlow(Long accountId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions;
        if (accountId != null) {
            transactions = transactionRepository.listByAccountAndDateRangeAsc(accountId, from, to);
        } else {
            transactions = transactionRepository.listByDateRangeAsc(from, to);
        }

        Map<LocalDate, BigDecimal> dailyNet = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.date,
                        Collectors.reducing(BigDecimal.ZERO,
                                t -> t.type == TransactionType.INCOME ? t.amount : t.amount.negate(),
                                BigDecimal::add)));

        BigDecimal running = BigDecimal.ZERO;
        List<DailyBalance> result = new java.util.ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            BigDecimal net = dailyNet.getOrDefault(d, BigDecimal.ZERO);
            running = running.add(net);
            if (dailyNet.containsKey(d)) {
                result.add(new DailyBalance(d, net, running));
            }
        }
        return result;
    }

    public record MonthlySummary(
            LocalDate from,
            LocalDate to,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance) {
    }

    public record CategoryBreakdown(String categoryName, BigDecimal total) {
    }

    public record DailyBalance(LocalDate date, BigDecimal netAmount, BigDecimal runningBalance) {
    }
}
