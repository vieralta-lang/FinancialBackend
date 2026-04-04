package org.acme.category;

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

@Path("/categories")
@Tag(name = "Categories", description = "Manage income and expense categories")
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(summary = "List categories", description = "List all categories, optionally filtered by type")
    public List<Category> list(@Parameter(description = "Filter by INCOME or EXPENSE") @QueryParam("type") CategoryType type) {
        return categoryService.listAll(type);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get category by ID")
    @APIResponse(responseCode = "404", description = "Category not found")
    public Category get(@Parameter(description = "Category ID") @PathParam("id") Long id) {
        return categoryService.findById(id);
    }

    @POST
    @Operation(summary = "Create a new category")
    @APIResponse(responseCode = "201", description = "Category created")
    public Response create(@Valid Category category) {
        Category created = categoryService.create(category);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a category")
    @APIResponse(responseCode = "404", description = "Category not found")
    public Category update(@Parameter(description = "Category ID") @PathParam("id") Long id, @Valid Category updated) {
        return categoryService.update(id, updated);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a category")
    @APIResponse(responseCode = "204", description = "Category deleted")
    public Response delete(@Parameter(description = "Category ID") @PathParam("id") Long id) {
        categoryService.delete(id);
        return Response.noContent().build();
    }
}
