package org.acme.report;

import java.time.LocalDate;
import java.util.List;

import org.acme.transaction.TransactionType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/reports")
@Tag(name = "Reports", description = "Financial reports and analytics")
public class ReportResource {

    @Inject
    ReportService reportService;

    @GET
    @Path("/summary")
    @Operation(summary = "Monthly summary", description = "Total income vs. expenses for a date range")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public ReportService.MonthlySummary monthlySummary(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to) {
        return reportService.monthlySummary(from, to);
    }

    @GET
    @Path("/by-category")
    @Operation(summary = "Spending by category", description = "Breakdown of amounts grouped by category")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public List<ReportService.CategoryBreakdown> byCategory(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to,
            @Parameter(description = "Filter by transaction type") @QueryParam("type") TransactionType type) {
        return reportService.byCategory(from, to, type);
    }

    @GET
    @Path("/cash-flow")
    @Operation(summary = "Cash flow", description = "Daily running balance over a date range")
    @APIResponse(responseCode = "400", description = "Missing required date parameters")
    public List<ReportService.DailyBalance> cashFlow(
            @Parameter(description = "Filter by account ID") @QueryParam("accountId") Long accountId,
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) @QueryParam("to") LocalDate to) {
        return reportService.cashFlow(accountId, from, to);
    }
}
