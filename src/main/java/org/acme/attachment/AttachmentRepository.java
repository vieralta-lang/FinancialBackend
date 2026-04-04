package org.acme.attachment;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttachmentRepository implements PanacheRepository<Attachment> {

    public List<Attachment> findByEntity(String entityType, Long entityId) {
        return list("entityType = ?1 and entityId = ?2", entityType, entityId);
    }
}
