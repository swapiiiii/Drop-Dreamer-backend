package com.dropdreamer.backend.dto;

public class AddToCartRequest {

    private Long productId;
    private Integer quantity; // optional, default handled in controller

    public AddToCartRequest() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
