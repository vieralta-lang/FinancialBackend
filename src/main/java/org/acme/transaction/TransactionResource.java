package org.acme.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/transactions")
@Tag(name = "Transactions", description = "Manage financial transactions and transfers")
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @GET
    @Operation(summary = "List transactions", description = "Filter by accountId, type, and/or date range")
    public List<Transaction> list(
            @Parameter(description = "Filter by account ID") @QueryParam("accountId") Long accountId,
            @Parameter(description = "Filter by INCOME, EXPENSE or TRANSFER") @QueryParam("type") TransactionType type,
            @Parameter(description = "Start date (yyyy-MM-dd)") @QueryParam("from") LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)") @QueryParam("to") LocalDate to) {

        if (accountId != null && from != null && to != null) {
            return Transaction.list(
                    "account.id = ?1 and date >= ?2 and date <= ?3 order by date desc",
                    accountId, from, to);
        }
        if (accountId != null) {
            return Transaction.list("account.id = ?1 order by date desc", accountId);
        }
        if (type != null) {
            return Transaction.list("type = ?1 order by date desc", type);
        }
        if (from != null && to != null) {
            return Transaction.list("date >= ?1 and date <= ?2 order by date desc", from, to);
        }
        return Transaction.list("order by date desc");
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get transaction by ID")
    @APIResponse(responseCode = "404", description = "Transaction not found")
    public Transaction get(@Parameter(description = "Transaction ID") @PathParam("id") Long id) {
        Transaction transaction = Transaction.findById(id);
        if (transaction == null) {
            throw new WebApplicationException("Transaction not found", 404);
        }
        return transaction;
    }

    @POST
    @Operation(summary = "Create a transaction", description = "Creates a transaction and updates the account balance")
    @APIResponse(responseCode = "201", description = "Transaction created")
    public Response create(@Valid Transaction transaction) {
        Transaction created = transactionService.createTransaction(transaction);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/transfer")
    @Operation(summary = "Transfer between accounts", description = "Creates linked debit/credit transactions and updates both account balances")
    @APIResponse(responseCode = "201", description = "Transfer completed")
    public Response transfer(TransferRequest request) {
        var result = transactionService.createTransfer(
                request.fromAccountId, request.toAccountId,
                request.amount, request.description, request.date);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes the transaction and reverses the balance change. For transfers, deletes both sides.")
    @APIResponse(responseCode = "204", description = "Transaction deleted")
    public Response delete(@Parameter(description = "Transaction ID") @PathParam("id") Long id) {
        transactionService.deleteTransaction(id);
        return Response.noContent().build();
    }

    public static class TransferRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public BigDecimal amount;
        public String description;
        public LocalDate date;
    }
}
