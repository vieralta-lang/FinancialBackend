package org.acme.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.category.Category;
import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/budgets")
@Tag(name = "Budgets", description = "Manage spending budgets per category")
public class BudgetResource {

    @GET
    @Operation(summary = "List all budgets")
    public List<Budget> list() {
        return Budget.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get budget by ID")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public Budget get(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        Budget budget = Budget.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        return budget;
    }

    @POST
    @Transactional
    @Operation(summary = "Create a budget")
    @APIResponse(responseCode = "201", description = "Budget created")
    public Response create(@Valid Budget budget) {
        if (budget.category != null && budget.category.id != null) {
            budget.category = Category.findById(budget.category.id);
        }
        budget.persist();
        return Response.status(Response.Status.CREATED).entity(budget).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a budget")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public Budget update(@Parameter(description = "Budget ID") @PathParam("id") Long id, @Valid Budget updated) {
        Budget budget = Budget.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        budget.limitAmount = updated.limitAmount;
        budget.period = updated.period;
        budget.startDate = updated.startDate;
        if (updated.category != null && updated.category.id != null) {
            budget.category = Category.findById(updated.category.id);
        }
        budget.updatedAt = LocalDateTime.now();
        return budget;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a budget")
    @APIResponse(responseCode = "204", description = "Budget deleted")
    public Response delete(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        Budget budget = Budget.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }
        budget.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/status")
    @Operation(summary = "Get budget status", description = "Returns spent vs. limit for the current budget period")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public BudgetStatus getStatus(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        Budget budget = Budget.findById(id);
        if (budget == null) {
            throw new WebApplicationException("Budget not found", 404);
        }

        LocalDate periodStart = budget.startDate;
        LocalDate periodEnd = calculatePeriodEnd(periodStart, budget.period);

        // If current date is past the period, advance to current period
        LocalDate today = LocalDate.now();
        while (periodEnd.isBefore(today)) {
            periodStart = periodEnd.plusDays(1);
            periodEnd = calculatePeriodEnd(periodStart, budget.period);
        }

        BigDecimal spent = Transaction
                .find("category.id = ?1 and type = ?2 and date >= ?3 and date <= ?4",
                        budget.category.id, TransactionType.EXPENSE, periodStart, periodEnd)
                .stream()
                .map(t -> ((Transaction) t).amount)
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
            LocalDate periodEnd) {}
}
