package org.acme.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/attachments")
@Tag(name = "Attachments", description = "Upload and manage file attachments (receipts, proofs, etc.)")
public class AttachmentResource {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/octet-stream"
    );

    @ConfigProperty(name = "app.attachments.storage-dir", defaultValue = "attachments")
    String storageDir;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    @Operation(summary = "Upload an attachment",
            description = "Upload a file (image or PDF, max 10MB) linked to a transaction or debt")
    @APIResponse(responseCode = "201", description = "Attachment uploaded")
    @APIResponse(responseCode = "400", description = "Invalid file")
    public Response upload(
            @Parameter(description = "Entity type: transaction or debt") @QueryParam("entityType") String entityType,
            @Parameter(description = "Entity ID") @QueryParam("entityId") Long entityId,
            @org.jboss.resteasy.reactive.RestForm("file") FileUpload file) throws IOException {

        if (entityType == null || entityId == null) {
            throw new WebApplicationException("entityType and entityId are required", 400);
        }
        if (!List.of("transaction", "debt").contains(entityType)) {
            throw new WebApplicationException("entityType must be 'transaction' or 'debt'", 400);
        }
        if (file == null || file.fileName() == null) {
            throw new WebApplicationException("File is required", 400);
        }
        if (file.size() > MAX_FILE_SIZE) {
            throw new WebApplicationException("File too large (max 10MB)", 400);
        }

        String contentType = file.contentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            // fallback: check extension
            String name = file.fileName().toLowerCase();
            if (name.endsWith(".pdf")) contentType = "application/pdf";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (name.endsWith(".png")) contentType = "image/png";
            else throw new WebApplicationException("File type not allowed. Use images or PDF.", 400);
        }

        // Create storage directory
        Path storageRoot = Path.of(storageDir);
        Files.createDirectories(storageRoot);

        // Generate unique filename
        String ext = getExtension(file.fileName());
        String storedName = UUID.randomUUID() + ext;
        Path targetPath = storageRoot.resolve(storedName);

        // Copy uploaded file
        try (InputStream is = Files.newInputStream(file.uploadedFile())) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Attachment attachment = new Attachment();
        attachment.fileName = sanitizeFileName(file.fileName());
        attachment.contentType = contentType;
        attachment.fileSize = file.size();
        attachment.storagePath = storedName;
        attachment.entityType = entityType;
        attachment.entityId = entityId;
        attachment.persist();

        return Response.status(Response.Status.CREATED).entity(attachment).build();
    }

    @GET
    @Operation(summary = "List attachments for an entity")
    public List<Attachment> list(
            @Parameter(description = "Entity type: transaction or debt") @QueryParam("entityType") String entityType,
            @Parameter(description = "Entity ID") @QueryParam("entityId") Long entityId) {
        if (entityType != null && entityId != null) {
            return Attachment.findByEntity(entityType, entityId);
        }
        return Attachment.listAll();
    }

    @GET
    @jakarta.ws.rs.Path("/{id}/download")
    @Operation(summary = "Download an attachment file")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    public Response download(@Parameter(description = "Attachment ID") @PathParam("id") Long id) throws IOException {
        Attachment attachment = Attachment.findById(id);
        if (attachment == null) {
            throw new WebApplicationException("Attachment not found", 404);
        }

        Path filePath = Path.of(storageDir).resolve(attachment.storagePath);
        if (!Files.exists(filePath)) {
            throw new WebApplicationException("File not found on disk", 404);
        }

        return Response.ok(Files.readAllBytes(filePath))
                .header("Content-Disposition", "inline; filename=\"" + attachment.fileName + "\"")
                .header("Content-Type", attachment.contentType)
                .build();
    }

    @DELETE
    @jakarta.ws.rs.Path("/{id}")
    @Transactional
    @Operation(summary = "Delete an attachment")
    @APIResponse(responseCode = "204", description = "Attachment deleted")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    public Response delete(@Parameter(description = "Attachment ID") @PathParam("id") Long id) throws IOException {
        Attachment attachment = Attachment.findById(id);
        if (attachment == null) {
            throw new WebApplicationException("Attachment not found", 404);
        }

        // Delete file from disk
        Path filePath = Path.of(storageDir).resolve(attachment.storagePath);
        Files.deleteIfExists(filePath);

        attachment.delete();
        return Response.noContent().build();
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot).toLowerCase() : "";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
