package com.dropdreamer.backend.dto;

public class UpdateCartRequest {
    private Long productId;
    private int quantity;

    public UpdateCartRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
