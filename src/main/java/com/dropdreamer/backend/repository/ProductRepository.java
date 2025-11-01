package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
