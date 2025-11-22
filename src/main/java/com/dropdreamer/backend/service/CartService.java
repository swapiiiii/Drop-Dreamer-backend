package com.dropdreamer.backend.service;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.CartItem;
import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.CartItemRepository;
import com.dropdreamer.backend.repository.CartRepository;
import com.dropdreamer.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    // Add to cart for guest (session-based)
    @Transactional
    public Cart addToCartForGuest(String sessionId, Long productId, int quantity) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(cart, productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }

        return cart;
    }

    // Add to cart for logged-in user
    @Transactional
    public Cart addToCartForUser(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(cart, productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }

        return cart;
    }

    // Merge guest cart into logged-in user cart
    @Transactional
    public Cart mergeGuestCartWithUser(String sessionId, Long userId) {
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart userCart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        if (guestCartOpt.isEmpty()) {
            return userCart; // no guest cart to merge
        }

        Cart guestCart = guestCartOpt.get();
        List<CartItem> guestItems = cartItemRepository.findByCart(guestCart);

        for (CartItem guestItem : guestItems) {
            Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(userCart, guestItem.getProductId());
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                guestItem.setCart(userCart);
                cartItemRepository.save(guestItem);
            }
        }

        // Delete guest cart after merging
        cartRepository.delete(guestCart);

        return userCart;
    }
}
