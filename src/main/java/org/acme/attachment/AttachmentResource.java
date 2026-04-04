package org.acme.attachment;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/attachments")
@Tag(name = "Attachments", description = "Upload and manage file attachments (receipts, proofs, etc.)")
public class AttachmentResource {

    @Inject
    AttachmentService attachmentService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload an attachment",
            description = "Upload a file (image or PDF, max 10MB) linked to a transaction or debt")
    @APIResponse(responseCode = "201", description = "Attachment uploaded")
    @APIResponse(responseCode = "400", description = "Invalid file")
    public Response upload(
            @Parameter(description = "Entity type: transaction or debt") @QueryParam("entityType") String entityType,
            @Parameter(description = "Entity ID") @QueryParam("entityId") Long entityId,
            @org.jboss.resteasy.reactive.RestForm("file") FileUpload file) throws IOException {
        Attachment attachment = attachmentService.upload(entityType, entityId, file);
        return Response.status(Response.Status.CREATED).entity(attachment).build();
    }

    @GET
    @Operation(summary = "List attachments for an entity")
    public List<Attachment> list(
            @Parameter(description = "Entity type: transaction or debt") @QueryParam("entityType") String entityType,
            @Parameter(description = "Entity ID") @QueryParam("entityId") Long entityId) {
        return attachmentService.list(entityType, entityId);
    }

    @GET
    @jakarta.ws.rs.Path("/{id}/download")
    @Operation(summary = "Download an attachment file")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    public Response download(@Parameter(description = "Attachment ID") @PathParam("id") Long id) throws IOException {
        AttachmentService.DownloadResult result = attachmentService.download(id);
        return Response.ok(result.content())
                .header("Content-Disposition", "inline; filename=\"" + result.attachment().fileName + "\"")
                .header("Content-Type", result.attachment().contentType)
                .build();
    }

    @DELETE
    @jakarta.ws.rs.Path("/{id}")
    @Operation(summary = "Delete an attachment")
    @APIResponse(responseCode = "204", description = "Attachment deleted")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    public Response delete(@Parameter(description = "Attachment ID") @PathParam("id") Long id) throws IOException {
        attachmentService.delete(id);
        return Response.noContent().build();
    }
}
