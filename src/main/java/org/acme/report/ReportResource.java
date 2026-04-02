package org.acme.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;

@Path("/reports")
@Tag(name = "Reports", description = "Financial reports and analytics")
public class ReportResource {

    @GET
    @Path("/summary")
    @Operation(summary = "Monthly summary", description = "Total income vs. expenses for a date range")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public MonthlySummary monthlySummary(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to) {

        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions = Transaction.list(
                "date >= ?1 and date <= ?2", from, to);

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

    @GET
    @Path("/by-category")
    @Operation(summary = "Spending by category", description = "Breakdown of amounts grouped by category")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public List<CategoryBreakdown> byCategory(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to,
            @Parameter(description = "Filter by transaction type") @QueryParam("type") TransactionType type) {

        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions;
        if (type != null) {
            transactions = Transaction.list(
                    "date >= ?1 and date <= ?2 and type = ?3", from, to, type);
        } else {
            transactions = Transaction.list("date >= ?1 and date <= ?2", from, to);
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

    @GET
    @Path("/cash-flow")
    @Operation(summary = "Cash flow", description = "Daily running balance over a date range")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public List<DailyBalance> cashFlow(
            @Parameter(description = "Filter by account ID") @QueryParam("accountId") Long accountId,
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to) {

        if (from == null || to == null) {
            throw new WebApplicationException("'from' and 'to' query params are required", 400);
        }

        List<Transaction> transactions;
        if (accountId != null) {
            transactions = Transaction.list(
                    "account.id = ?1 and date >= ?2 and date <= ?3 order by date asc",
                    accountId, from, to);
        } else {
            transactions = Transaction.list(
                    "date >= ?1 and date <= ?2 order by date asc", from, to);
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
            BigDecimal balance) {}

    public record CategoryBreakdown(String categoryName, BigDecimal total) {}

    public record DailyBalance(LocalDate date, BigDecimal netAmount, BigDecimal runningBalance) {}
}
