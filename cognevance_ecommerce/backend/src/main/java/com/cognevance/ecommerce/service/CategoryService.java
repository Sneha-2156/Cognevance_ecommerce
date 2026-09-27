package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.entity.Category;
import com.cognevance.ecommerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() { return categoryRepository.findAll(); }

    public Category create(Category category) { return categoryRepository.save(category); }

    public void delete(Long id) { categoryRepository.deleteById(id); }
}
