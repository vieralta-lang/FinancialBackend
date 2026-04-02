package org.acme.category;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/categories")
@Tag(name = "Categories", description = "Manage income and expense categories")
public class CategoryResource {

    @GET
    @Operation(summary = "List categories", description = "List all categories, optionally filtered by type")
    public List<Category> list(@Parameter(description = "Filter by INCOME or EXPENSE") @QueryParam("type") CategoryType type) {
        if (type != null) {
            return Category.list("type", type);
        }
        return Category.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get category by ID")
    @APIResponse(responseCode = "404", description = "Category not found")
    public Category get(@Parameter(description = "Category ID") @PathParam("id") Long id) {
        Category category = Category.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        return category;
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new category")
    @APIResponse(responseCode = "201", description = "Category created")
    public Response create(@Valid Category category) {
        if (category.parentCategory != null && category.parentCategory.id != null) {
            category.parentCategory = Category.findById(category.parentCategory.id);
        }
        category.persist();
        return Response.status(Response.Status.CREATED).entity(category).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update a category")
    @APIResponse(responseCode = "404", description = "Category not found")
    public Category update(@Parameter(description = "Category ID") @PathParam("id") Long id, @Valid Category updated) {
        Category category = Category.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        category.name = updated.name;
        category.type = updated.type;
        category.icon = updated.icon;
        category.color = updated.color;
        if (updated.parentCategory != null && updated.parentCategory.id != null) {
            category.parentCategory = Category.findById(updated.parentCategory.id);
        } else {
            category.parentCategory = null;
        }
        category.updatedAt = LocalDateTime.now();
        return category;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a category")
    @APIResponse(responseCode = "204", description = "Category deleted")
    public Response delete(@Parameter(description = "Category ID") @PathParam("id") Long id) {
        Category category = Category.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        category.delete();
        return Response.noContent().build();
    }
}
