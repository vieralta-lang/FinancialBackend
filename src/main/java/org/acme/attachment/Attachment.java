package org.acme.attachment;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "attachments")
public class Attachment extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String fileName;

    @NotBlank
    @Column(nullable = false)
    public String contentType;

    @Column(nullable = false)
    public long fileSize;

    /**
     * Path to the stored file on disk (relative to storage root).
     */
    @NotBlank
    @Column(nullable = false, unique = true)
    public String storagePath;

    /**
     * Type of the entity this attachment belongs to, e.g. "transaction", "debt".
     */
    @NotNull
    @Column(nullable = false)
    public String entityType;

    /**
     * ID of the entity this attachment belongs to.
     */
    @NotNull
    @Column(nullable = false)
    public Long entityId;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public static java.util.List<Attachment> findByEntity(String entityType, Long entityId) {
        return list("entityType = ?1 and entityId = ?2", entityType, entityId);
    }
}
