package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.entity.Product;
import com.dropdreamer.backend.entity.Admin;
import com.dropdreamer.backend.repository.AdminRepository;
import com.dropdreamer.backend.repository.ProductRepository;
import com.dropdreamer.backend.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    public ProductController(ProductRepository productRepository,
                             AdminRepository adminRepository,
                             JwtUtil jwtUtil) {
        this.productRepository = productRepository;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
    }

    // ---------------------------------------
    // CHECK ADMIN AUTHORIZATION
    // ---------------------------------------
    private boolean isAdminAuthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (jwtUtil.isTokenExpired(token)) {
                return false;
            }

            return adminRepository.findByEmail(email).isPresent();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ---------------------------------------
    // ADD PRODUCT (ADMIN ONLY)
    // ---------------------------------------
    @PostMapping("/add")
    public Map<String, Object> addProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Product product) {

        if (!isAdminAuthorized(authHeader)) {
            return Map.of("message", "❌ Unauthorized: Only admin can add products");
        }

        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);

        return Map.of("message", "✅ Product added successfully", "product", product);
    }

    // ---------------------------------------
    // GET PRODUCTS (FILTERS WORK PERFECTLY)
    // ---------------------------------------
    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false, name = "mainCategory") String mainCategory,
            @RequestParam(required = false, name = "subCategory") String subCategory,
            @RequestParam(required = false, name = "search") String search
    ) {

        // 1️⃣ Search by product name
        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(search);
        }

        // 2️⃣ Filter by main + sub category
        if (mainCategory != null && subCategory != null) {
            return productRepository.findByMainCategoryAndSubCategory(mainCategory, subCategory);
        }

        // 3️⃣ Filter by main category only
        if (mainCategory != null) {
            return productRepository.findByMainCategory(mainCategory);
        }

        // 4️⃣ Filter by sub category only
        if (subCategory != null) {
            return productRepository.findBySubCategory(subCategory);
        }

        // 5️⃣ Default → all products
        return productRepository.findAll();
    }

    // ---------------------------------------
    // GET SINGLE PRODUCT
    // ---------------------------------------
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // ---------------------------------------
    // UPDATE PRODUCT (ADMIN ONLY)
    // ---------------------------------------
    @PutMapping("/{id}")
    public Map<String, String> updateProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        if (!isAdminAuthorized(authHeader)) {
            return Map.of("message", "❌ Unauthorized: Only admin can update products");
        }

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return Map.of("message", "Product not found");
        }

        Product existingProduct = productOpt.get();

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setMainCategory(updatedProduct.getMainCategory());
        existingProduct.setSubCategory(updatedProduct.getSubCategory());

        existingProduct.setImageUrl1(updatedProduct.getImageUrl1());
        existingProduct.setImageUrl2(updatedProduct.getImageUrl2());
        existingProduct.setImageUrl3(updatedProduct.getImageUrl3());
        existingProduct.setImageUrl4(updatedProduct.getImageUrl4());
        existingProduct.setImageUrl5(updatedProduct.getImageUrl5());

        existingProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(existingProduct);

        return Map.of("message", "✅ Product updated successfully");
    }

    // ---------------------------------------
    // DELETE PRODUCT (ADMIN ONLY)
    // ---------------------------------------
    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        if (!isAdminAuthorized(authHeader)) {
            return Map.of("message", "❌ Unauthorized: Only admin can delete products");
        }

        if (!productRepository.existsById(id)) {
            return Map.of("message", "Product not found");
        }

        productRepository.deleteById(id);

        return Map.of("message", "✅ Product deleted successfully");
    }
}
