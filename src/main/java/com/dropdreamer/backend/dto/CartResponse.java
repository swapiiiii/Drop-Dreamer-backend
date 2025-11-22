package com.dropdreamer.backend.dto;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {

    private Long cartId;
    private String sessionId;
    private List<CartItemResponse> items;

    public CartResponse() {}

    public CartResponse(Long cartId, String sessionId, List<CartItemResponse> items) {
        this.cartId = cartId;
        this.sessionId = sessionId;
        this.items = items;
    }

    public static CartResponse fromCart(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                .map(ci -> new CartItemResponse(
                        ci.getCartItemId(),
                        ci.getProductId(),  // Use getProductId() instead of getProduct()
                        ci.getQuantity()
                ))
                .collect(Collectors.toList());

        return new CartResponse(
                cart.getCartId(),
                cart.getSessionId(),
                itemResponses
        );
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
}
