package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.entity.Category;
import com.cognevance.ecommerce.entity.Product;
import com.cognevance.ecommerce.repository.CategoryRepository;
import com.cognevance.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll(String search, Long categoryId) {
        if (search != null && !search.isBlank()) return productRepository.search(search);
        if (categoryId != null) return productRepository.findByCategoryId(categoryId);
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product create(Product product, Long categoryId) {
        attachCategory(product, categoryId);
        return productRepository.save(product);
    }

    public Product update(Long id, Product updated, Long categoryId) {
        Product existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        if (updated.getImageUrl() != null) existing.setImageUrl(updated.getImageUrl());
        attachCategory(existing, categoryId);
        return productRepository.save(existing);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    // Called during checkout to atomically check + reduce stock
    public void reduceStock(Long productId, int quantity) {
        Product product = findById(productId);
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private void attachCategory(Product product, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
            product.setCategory(category);
        }
    }
}
