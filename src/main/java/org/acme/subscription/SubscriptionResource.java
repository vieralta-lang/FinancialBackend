package org.acme.subscription;

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

@Path("/subscriptions")
@Tag(name = "Subscriptions", description = "Manage recurring subscriptions (streaming, software, etc.)")
public class SubscriptionResource {

    @Inject
    SubscriptionService subscriptionService;

    @GET
    @Operation(summary = "List all subscriptions")
    public List<Subscription> list() {
        return subscriptionService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get subscription by ID")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Subscription get(@Parameter(description = "Subscription ID") @PathParam("id") Long id) {
        return subscriptionService.findById(id);
    }

    @POST
    @Operation(summary = "Create a new subscription")
    @APIResponse(responseCode = "201", description = "Subscription created")
    public Response create(@Valid Subscription subscription) {
        Subscription created = subscriptionService.create(subscription);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a subscription")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Subscription update(@Parameter(description = "Subscription ID") @PathParam("id") Long id, @Valid Subscription updated) {
        return subscriptionService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a subscription")
    @APIResponse(responseCode = "204", description = "Subscription deleted")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Response delete(@Parameter(description = "Subscription ID") @PathParam("id") Long id) {
        subscriptionService.delete(id);
        return Response.noContent().build();
    }
}
