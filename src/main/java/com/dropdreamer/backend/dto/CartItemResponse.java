package com.dropdreamer.backend.dto;

public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private int quantity;

    public CartItemResponse() {}

    public CartItemResponse(Long cartItemId, Long productId, int quantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
