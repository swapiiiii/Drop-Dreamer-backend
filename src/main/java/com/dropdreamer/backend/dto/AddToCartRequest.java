package com.dropdreamer.backend.dto;

public class AddToCartRequest {
    private Long productId;
    private Integer quantity; // optional, default to 1 if null
    private String sessionId; // for guest users
    private Long userId;      // for logged-in users

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

    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
