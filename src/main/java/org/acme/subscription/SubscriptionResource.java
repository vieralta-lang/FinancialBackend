package org.acme.subscription;

import java.time.LocalDateTime;
import java.util.List;

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

@Path("/subscriptions")
@Tag(name = "Subscriptions", description = "Manage recurring subscriptions (streaming, software, etc.)")
public class SubscriptionResource {

    @GET
    @Operation(summary = "List all subscriptions")
    public List<Subscription> list() {
        return Subscription.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get subscription by ID")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Subscription get(@Parameter(description = "Subscription ID") @PathParam("id") Long id) {
        Subscription sub = Subscription.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        return sub;
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new subscription")
    @APIResponse(responseCode = "201", description = "Subscription created")
    public Response create(@Valid Subscription subscription) {
        subscription.persist();
        return Response.status(Response.Status.CREATED).entity(subscription).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a subscription")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Subscription update(@Parameter(description = "Subscription ID") @PathParam("id") Long id, @Valid Subscription updated) {
        Subscription sub = Subscription.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        sub.name = updated.name;
        sub.amount = updated.amount;
        sub.frequency = updated.frequency;
        sub.category = updated.category;
        sub.nextBillingDate = updated.nextBillingDate;
        sub.active = updated.active;
        sub.notes = updated.notes;
        sub.updatedAt = LocalDateTime.now();
        return sub;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a subscription")
    @APIResponse(responseCode = "204", description = "Subscription deleted")
    @APIResponse(responseCode = "404", description = "Subscription not found")
    public Response delete(@Parameter(description = "Subscription ID") @PathParam("id") Long id) {
        Subscription sub = Subscription.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        sub.delete();
        return Response.noContent().build();
    }
}
