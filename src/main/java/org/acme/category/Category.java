package org.acme.category;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "categories")
public class Category extends PanacheEntity {

    @NotBlank
    @Column(nullable = false)
    public String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CategoryType type;

    public String icon;

    public String color;

    @ManyToOne(fetch = FetchType.LAZY)
    public Category parentCategory;

    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}
