package com.dropdreamer.backend.dto;

public class UpdateCartRequest {
    private Long productId;
    private Integer quantity;

    public UpdateCartRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
