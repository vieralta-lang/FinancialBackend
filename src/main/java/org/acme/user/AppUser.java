package org.acme.user;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.Account;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "app_users")
public class AppUser extends PanacheEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    public String name;

    @Column(unique = true)
    public String username;

    public String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String passwordHash;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    public List<Account> accounts;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
