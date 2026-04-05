package org.acme.account;

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

@Path("/accounts")
@Tag(name = "Accounts", description = "Manage financial accounts (checking, savings, credit card, etc.)")
public class AccountResource {

    @Inject
    AccountService accountService;

    @GET
    @Operation(summary = "List accounts", description = "List all accounts, optionally filtered by userId")
    public List<Account> list(
            @Parameter(description = "Filter by user ID") @QueryParam("userId") Long userId) {
        if (userId != null) {
            return accountService.listByUserId(userId);
        }
        return accountService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get account by ID")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Account get(@Parameter(description = "Account ID") @PathParam("id") Long id) {
        return accountService.findById(id);
    }

    @POST
    @Operation(summary = "Create a new account")
    @APIResponse(responseCode = "201", description = "Account created")
    public Response create(@Valid Account account) {
        Account created = accountService.create(account);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an account")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Account update(@Parameter(description = "Account ID") @PathParam("id") Long id, @Valid Account updated) {
        return accountService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete an account")
    @APIResponse(responseCode = "204", description = "Account deleted")
    @APIResponse(responseCode = "404", description = "Account not found")
    public Response delete(@Parameter(description = "Account ID") @PathParam("id") Long id) {
        accountService.delete(id);
        return Response.noContent().build();
    }
}
