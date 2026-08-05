package com.stacko.mall.application.service;

import com.stacko.mall.application.command.CreateProductCategoryCommand;
import com.stacko.mall.application.command.UpdateProductCategoryCommand;
import com.stacko.mall.domain.enums.ProductCategoryStatus;
import com.stacko.mall.domain.model.ProductCategory;
import com.stacko.mall.domain.model.ProductCategoryId;
import com.stacko.mall.domain.repository.ProductCategoryRepository;
import com.stacko.mall.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductCategoryApplicationService {
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductCategoryApplicationService(ProductCategoryRepository categoryRepository,
                                             ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductCategory create(CreateProductCategoryCommand command) {
        ProductCategory parent = resolveParent(command.getTenantId(), command.getParentId());
        ProductCategory category = ProductCategory.create(
                command.getTenantId(),
                parent == null ? null : parent.getId().value(),
                command.getName(),
                normalizeSort(command.getSort()),
                command.getStatus() == null ? ProductCategoryStatus.ENABLED : command.getStatus(),
                parent == null ? 1 : parent.getLevel() + 1,
                parent == null ? null : parent.getPath()
        );
        return categoryRepository.save(category);
    }

    @Transactional
    public ProductCategory update(UpdateProductCategoryCommand command) {
        ProductCategory category = get(command.getTenantId(), command.getCategoryId());
        ProductCategory parent = resolveParent(command.getTenantId(), command.getParentId());
        if (parent != null && parent.getId().equals(category.getId())) {
            throw new IllegalArgumentException("Category cannot use itself as parent");
        }
        if (parent != null && parent.getPath().contains(category.getId().value())) {
            throw new IllegalArgumentException("Category parent cannot be its child");
        }
        category.update(
                parent == null ? null : parent.getId().value(),
                command.getName(),
                normalizeSort(command.getSort()),
                command.getStatus() == null ? ProductCategoryStatus.ENABLED : command.getStatus(),
                parent == null ? 1 : parent.getLevel() + 1,
                parent == null ? null : parent.getPath()
        );
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(String tenantId, String categoryId) {
        if (categoryRepository.countByParentId(tenantId, categoryId) > 0) {
            throw new IllegalArgumentException("Category has children");
        }
        if (productRepository.countByCategory(tenantId, categoryId) > 0) {
            throw new IllegalArgumentException("Category has products");
        }
        categoryRepository.delete(tenantId, new ProductCategoryId(categoryId));
    }

    public ProductCategory get(String tenantId, String categoryId) {
        return categoryRepository.findById(tenantId, new ProductCategoryId(categoryId))
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public ProductCategory getEnabled(String tenantId, String categoryId) {
        ProductCategory category = get(tenantId, categoryId);
        if (!category.isEnabled()) {
            throw new IllegalArgumentException("Category disabled");
        }
        return category;
    }

    public List<ProductCategory> list(String tenantId) {
        return categoryRepository.listByTenant(tenantId).stream()
                .sorted(Comparator.comparing(ProductCategory::getParentId, Comparator.nullsFirst(String::compareTo))
                        .thenComparing(ProductCategory::getSort)
                        .thenComparing(ProductCategory::getCreatedAt))
                .toList();
    }

    public List<ProductCategory> listEnabled(String tenantId) {
        return list(tenantId).stream()
                .filter(ProductCategory::isEnabled)
                .toList();
    }

    private ProductCategory resolveParent(String tenantId, String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return get(tenantId, parentId);
    }

    private int normalizeSort(Integer sort) {
        return sort == null ? 0 : sort;
    }
}
