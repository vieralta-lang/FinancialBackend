package org.acme.user;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "app_users")
public class AppUser extends PanacheEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    public String name;

    public String email;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();

    public static AppUser findByName(String name) {
        return find("name", name).firstResult();
    }
}
