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

    public ProductController(ProductRepository productRepository, AdminRepository adminRepository, JwtUtil jwtUtil) {
        this.productRepository = productRepository;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Strict admin validation
    private boolean isAdminAuthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            // validate token structure + expiration
            if (jwtUtil.isTokenExpired(token)) {
                return false;
            }

            // check if admin exists in DB
            return adminRepository.findByEmail(email).isPresent();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Add new product (Admin only)
    @PostMapping("/add")
    public Map<String, Object> addProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Product product) {

        System.out.println("🔍 Entered addProduct() - Auth header: " + authHeader);

        if (!isAdminAuthorized(authHeader)) {
            return Map.of("message", "❌ Unauthorized: Only admin can add products");
        }

        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);
        return Map.of("message", "✅ Product added successfully", "product", product);
    }

    // ✅ Fetch all products (Public)
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Fetch single product (Public)
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // ✅ Update product (Admin only)
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
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setImageUrl1(updatedProduct.getImageUrl1());
        existingProduct.setImageUrl2(updatedProduct.getImageUrl2());
        existingProduct.setImageUrl3(updatedProduct.getImageUrl3());
        existingProduct.setImageUrl4(updatedProduct.getImageUrl4());
        existingProduct.setImageUrl5(updatedProduct.getImageUrl5());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        productRepository.save(existingProduct);
        return Map.of("message", "✅ Product updated successfully");
    }

    // ✅ Delete product (Admin only)
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
