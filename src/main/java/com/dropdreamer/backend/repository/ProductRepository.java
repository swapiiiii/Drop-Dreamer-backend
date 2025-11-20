package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByMainCategory(String mainCategory);

    List<Product> findBySubCategory(String subCategory);

    List<Product> findByMainCategoryAndSubCategory(String mainCategory, String subCategory);

    List<Product> findByNameContainingIgnoreCase(String name);
}
