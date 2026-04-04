package org.acme.user;

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

@Path("/users")
@Tag(name = "Users", description = "Manage application users")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "List all users")
    public List<AppUser> list() {
        return userService.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get user by ID")
    @APIResponse(responseCode = "404", description = "User not found")
    public AppUser get(@Parameter(description = "User ID") @PathParam("id") Long id) {
        return userService.findById(id);
    }

    @POST
    @Operation(summary = "Create a new user")
    @APIResponse(responseCode = "201", description = "User created")
    public Response create(@Valid AppUser user) {
        AppUser created = userService.create(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a user")
    @APIResponse(responseCode = "404", description = "User not found")
    public AppUser update(@Parameter(description = "User ID") @PathParam("id") Long id, @Valid AppUser updated) {
        return userService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a user")
    @APIResponse(responseCode = "204", description = "User deleted")
    @APIResponse(responseCode = "404", description = "User not found")
    public Response delete(@Parameter(description = "User ID") @PathParam("id") Long id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
