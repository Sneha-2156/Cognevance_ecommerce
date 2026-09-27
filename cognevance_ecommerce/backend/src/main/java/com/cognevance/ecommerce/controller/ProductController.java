package com.cognevance.ecommerce.controller;

import com.cognevance.ecommerce.entity.Product;
import com.cognevance.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products?search=laptop&categoryId=2  (both optional, public)
    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) Long categoryId) {
        return productService.findAll(search, categoryId);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }

    // Admin only
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product,
                                                  @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product, categoryId));
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product,
                                  @RequestParam(required = false) Long categoryId) {
        return productService.update(id, product, categoryId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
