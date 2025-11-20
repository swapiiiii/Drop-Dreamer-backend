package com.dropdreamer.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", schema = "drop_dreamer")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double price;

    // OLD CATEGORY (shoes, accessories etc.) → treat as subCategory
    private String subCategory;

    // NEW main category (cricket, football etc.)
    private String mainCategory;

    private Integer stock;

    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -----------------------
    // Getters and Setters
    // -----------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getMainCategory() { return mainCategory; }
    public void setMainCategory(String mainCategory) { this.mainCategory = mainCategory; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImageUrl1() { return imageUrl1; }
    public void setImageUrl1(String imageUrl1) { this.imageUrl1 = imageUrl1; }

    public String getImageUrl2() { return imageUrl2; }
    public void setImageUrl2(String imageUrl2) { this.imageUrl2 = imageUrl2; }

    public String getImageUrl3() { return imageUrl3; }
    public void setImageUrl3(String imageUrl3) { this.imageUrl3 = imageUrl3; }

    public String getImageUrl4() { return imageUrl4; }
    public void setImageUrl4(String imageUrl4) { this.imageUrl4 = imageUrl4; }

    public String getImageUrl5() { return imageUrl5; }
    public void setImageUrl5(String imageUrl5) { this.imageUrl5 = imageUrl5; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
