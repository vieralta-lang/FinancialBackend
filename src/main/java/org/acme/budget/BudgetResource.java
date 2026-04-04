package org.acme.budget;

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
import jakarta.ws.rs.core.Response;

@Path("/budgets")
@Tag(name = "Budgets", description = "Manage spending budgets per category")
public class BudgetResource {

    @Inject
    BudgetService budgetService;

    @GET
    @Operation(summary = "List all budgets")
    public List<Budget> list() {
        return budgetService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get budget by ID")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public Budget get(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        return budgetService.findById(id);
    }

    @POST
    @Operation(summary = "Create a budget")
    @APIResponse(responseCode = "201", description = "Budget created")
    public Response create(@Valid Budget budget) {
        Budget created = budgetService.create(budget);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a budget")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public Budget update(@Parameter(description = "Budget ID") @PathParam("id") Long id, @Valid Budget updated) {
        return budgetService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a budget")
    @APIResponse(responseCode = "204", description = "Budget deleted")
    public Response delete(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        budgetService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/status")
    @Operation(summary = "Get budget status", description = "Returns spent vs. limit for the current budget period")
    @APIResponse(responseCode = "404", description = "Budget not found")
    public BudgetService.BudgetStatus getStatus(@Parameter(description = "Budget ID") @PathParam("id") Long id) {
        return budgetService.getStatus(id);
    }
}
