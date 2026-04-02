package org.acme.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

@Path("/accounts")
@Tag(name = "Accounts", description = "Manage financial accounts (checking, savings, credit card, etc.)")
public class AccountResource {

    @GET
    @Operation(summary = "List all accounts")
    @Transactional
    public List<Account> list() {
        List<Account> accounts = Account.listAll();
        accounts.forEach(this::recalculate);
        return accounts;
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get account by ID")
    @APIResponse(responseCode = "404", description = "Account not found")
    @Transactional
    public Account get(@Parameter(description = "Account ID") @PathParam("id") Long id) {
        Account account = Account.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        recalculate(account);
        return account;
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new account")
    @APIResponse(responseCode = "201", description = "Account created")
    public Response create(@Valid Account account) {
        account.currentBalance = account.initialBalance;
        account.persist();
        return Response.status(Response.Status.CREATED).entity(account).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update an account")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Account update(@Parameter(description = "Account ID") @PathParam("id") Long id, @Valid Account updated) {
        Account account = Account.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        account.name = updated.name;
        account.type = updated.type;
        account.currency = updated.currency;
        account.active = updated.active;
        account.updatedAt = LocalDateTime.now();
        return account;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete an account")
    @APIResponse(responseCode = "204", description = "Account deleted")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Response delete(@Parameter(description = "Account ID") @PathParam("id") Long id) {
        Account account = Account.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        account.delete();
        return Response.noContent().build();
    }

    /**
     * Recalculates currentBalance from initialBalance + all transactions.
     */
    private void recalculate(Account account) {
        List<Transaction> transactions = Transaction.list("account.id", account.id);
        BigDecimal balance = account.initialBalance;
        for (Transaction tx : transactions) {
            switch (tx.type) {
                case INCOME -> balance = balance.add(tx.amount);
                case EXPENSE -> balance = balance.subtract(tx.amount);
                case TRANSFER -> {
                    // Check if this account is the source or destination
                    // For transfers, debit side has description starting with "Transfer to"
                    // and credit side "Transfer from", but safer to check transferId pairs
                    if (tx.description != null && tx.description.startsWith("Transfer from")) {
                        balance = balance.add(tx.amount);
                    } else {
                        balance = balance.subtract(tx.amount);
                    }
                }
            }
        }
        if (account.currentBalance.compareTo(balance) != 0) {
            account.currentBalance = balance;
            account.updatedAt = LocalDateTime.now();
        }
    }
}
