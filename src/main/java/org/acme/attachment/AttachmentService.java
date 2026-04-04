package org.acme.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/octet-stream");

    @ConfigProperty(name = "app.attachments.storage-dir", defaultValue = "attachments")
    String storageDir;

    @Inject
    AttachmentRepository attachmentRepository;

    @Transactional
    public Attachment upload(String entityType, Long entityId, FileUpload file) throws IOException {
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
            String name = file.fileName().toLowerCase();
            if (name.endsWith(".pdf"))
                contentType = "application/pdf";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
                contentType = "image/jpeg";
            else if (name.endsWith(".png"))
                contentType = "image/png";
            else
                throw new WebApplicationException("File type not allowed. Use images or PDF.", 400);
        }

        Path storageRoot = Path.of(storageDir);
        Files.createDirectories(storageRoot);

        String ext = getExtension(file.fileName());
        String storedName = UUID.randomUUID() + ext;
        Path targetPath = storageRoot.resolve(storedName);

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
        attachmentRepository.persist(attachment);

        return attachment;
    }

    public List<Attachment> list(String entityType, Long entityId) {
        if (entityType != null && entityId != null) {
            return attachmentRepository.findByEntity(entityType, entityId);
        }
        return attachmentRepository.listAll();
    }

    public DownloadResult download(Long id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id);
        if (attachment == null) {
            throw new WebApplicationException("Attachment not found", 404);
        }

        Path filePath = Path.of(storageDir).resolve(attachment.storagePath);
        if (!Files.exists(filePath)) {
            throw new WebApplicationException("File not found on disk", 404);
        }

        return new DownloadResult(attachment, Files.readAllBytes(filePath));
    }

    @Transactional
    public void delete(Long id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id);
        if (attachment == null) {
            throw new WebApplicationException("Attachment not found", 404);
        }

        Path filePath = Path.of(storageDir).resolve(attachment.storagePath);
        Files.deleteIfExists(filePath);

        attachmentRepository.delete(attachment);
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot).toLowerCase() : "";
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    public record DownloadResult(Attachment attachment, byte[] content) {
    }
}
