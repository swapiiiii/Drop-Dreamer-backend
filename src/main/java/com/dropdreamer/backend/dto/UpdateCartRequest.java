package com.dropdreamer.backend.dto;

public class UpdateCartRequest {
    private Long productId;
    private Integer quantity;  // Must be Integer to allow null

    public UpdateCartRequest() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    // Corrected getter and setter to use Integer
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
