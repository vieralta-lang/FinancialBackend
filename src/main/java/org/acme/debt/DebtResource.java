package org.acme.debt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.Account;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/debts")
@Tag(name = "Debts", description = "Manage personal debts (money owed to/from others)")
public class DebtResource {

    @GET
    @Operation(summary = "List all debts")
    public List<Debt> list() {
        return Debt.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get debt by ID")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Debt get(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        Debt debt = Debt.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        return debt;
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new debt")
    @APIResponse(responseCode = "201", description = "Debt created")
    public Response create(@Valid Debt debt) {
        debt.persist();
        return Response.status(Response.Status.CREATED).entity(debt).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a debt")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Debt update(@Parameter(description = "Debt ID") @PathParam("id") Long id, @Valid Debt updated) {
        Debt debt = Debt.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        debt.person = updated.person;
        debt.direction = updated.direction;
        debt.amount = updated.amount;
        debt.description = updated.description;
        debt.dueDate = updated.dueDate;
        debt.paid = updated.paid;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a debt")
    @APIResponse(responseCode = "204", description = "Debt deleted")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Response delete(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        Debt debt = Debt.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        debt.delete();
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/pay")
    @Transactional
    @Operation(summary = "Mark debt as paid and update account balance",
            description = "I_OWE: subtracts from account. OWES_ME: adds to account.")
    @APIResponse(responseCode = "200", description = "Debt paid")
    @APIResponse(responseCode = "404", description = "Debt or Account not found")
    @APIResponse(responseCode = "400", description = "Debt already paid")
    public Debt pay(
            @Parameter(description = "Debt ID") @PathParam("id") Long id,
            @Parameter(description = "Account ID to credit/debit") @QueryParam("accountId") Long accountId) {
        Debt debt = Debt.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        if (debt.paid) {
            throw new WebApplicationException("Debt is already paid", 400);
        }
        Account account = Account.findById(accountId);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }

        // Create transaction record
        Transaction tx = new Transaction();
        tx.account = account;
        tx.amount = debt.amount;
        tx.date = LocalDate.now();
        tx.type = (debt.direction == DebtDirection.I_OWE) ? TransactionType.EXPENSE : TransactionType.INCOME;
        tx.description = (debt.direction == DebtDirection.I_OWE)
                ? "Pagamento dívida: " + debt.person
                : "Recebimento dívida: " + debt.person;
        tx.persist();

        debt.paid = true;
        debt.paidWithAccount = account;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }

    @POST
    @Path("/{id}/reopen")
    @Transactional
    @Operation(summary = "Reopen a paid debt and reverse the account balance change")
    @APIResponse(responseCode = "200", description = "Debt reopened")
    @APIResponse(responseCode = "404", description = "Debt not found")
    @APIResponse(responseCode = "400", description = "Debt is not paid")
    public Debt reopen(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        Debt debt = Debt.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        if (!debt.paid) {
            throw new WebApplicationException("Debt is not paid", 400);
        }

        if (debt.paidWithAccount != null) {
            Account account = debt.paidWithAccount;

            // Remove the transaction created when paying
            String descPrefix = (debt.direction == DebtDirection.I_OWE)
                    ? "Pagamento dívida: " + debt.person
                    : "Recebimento dívida: " + debt.person;
            Transaction.delete("account = ?1 and description = ?2 and amount = ?3",
                    account, descPrefix, debt.amount);
        }

        debt.paid = false;
        debt.paidWithAccount = null;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }
}
