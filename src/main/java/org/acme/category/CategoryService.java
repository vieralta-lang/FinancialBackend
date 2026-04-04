package org.acme.category;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    public List<Category> listAll(CategoryType type) {
        if (type != null) {
            return categoryRepository.listByType(type);
        }
        return categoryRepository.listAll();
    }

    public Category findById(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        return category;
    }

    @Transactional
    public Category create(Category category) {
        if (category.parentCategory != null && category.parentCategory.id != null) {
            category.parentCategory = categoryRepository.findById(category.parentCategory.id);
        }
        categoryRepository.persist(category);
        return category;
    }

    @Transactional
    public Category update(Long id, Category updated) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        category.name = updated.name;
        category.type = updated.type;
        category.icon = updated.icon;
        category.color = updated.color;
        if (updated.parentCategory != null && updated.parentCategory.id != null) {
            category.parentCategory = categoryRepository.findById(updated.parentCategory.id);
        } else {
            category.parentCategory = null;
        }
        category.updatedAt = LocalDateTime.now();
        return category;
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category not found", 404);
        }
        categoryRepository.delete(category);
    }
}
