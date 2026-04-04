package org.acme.debt;

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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/debts")
@Tag(name = "Debts", description = "Manage personal debts (money owed to/from others)")
public class DebtResource {

    @Inject
    DebtService debtService;

    @GET
    @Operation(summary = "List all debts")
    public List<Debt> list() {
        return debtService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get debt by ID")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Debt get(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        return debtService.findById(id);
    }

    @POST
    @Operation(summary = "Create a new debt")
    @APIResponse(responseCode = "201", description = "Debt created")
    public Response create(@Valid Debt debt) {
        Debt created = debtService.create(debt);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a debt")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Debt update(@Parameter(description = "Debt ID") @PathParam("id") Long id, @Valid Debt updated) {
        return debtService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a debt")
    @APIResponse(responseCode = "204", description = "Debt deleted")
    @APIResponse(responseCode = "404", description = "Debt not found")
    public Response delete(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        debtService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/pay")
    @Operation(summary = "Mark debt as paid and update account balance",
            description = "I_OWE: subtracts from account. OWES_ME: adds to account.")
    @APIResponse(responseCode = "200", description = "Debt paid")
    @APIResponse(responseCode = "404", description = "Debt or Account not found")
    @APIResponse(responseCode = "400", description = "Debt already paid")
    public Debt pay(
            @Parameter(description = "Debt ID") @PathParam("id") Long id,
            @Parameter(description = "Account ID to credit/debit") @QueryParam("accountId") Long accountId) {
        return debtService.pay(id, accountId);
    }

    @POST
    @Path("/{id}/reopen")
    @Operation(summary = "Reopen a paid debt and reverse the account balance change")
    @APIResponse(responseCode = "200", description = "Debt reopened")
    @APIResponse(responseCode = "404", description = "Debt not found")
    @APIResponse(responseCode = "400", description = "Debt is not paid")
    public Debt reopen(@Parameter(description = "Debt ID") @PathParam("id") Long id) {
        return debtService.reopen(id);
    }
}
