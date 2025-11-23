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

    // Add to cart for guest
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

        prepareCartForDto(cart);
        return cart;
    }

    // Add to cart for user
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

        prepareCartForDto(cart);
        return cart;
    }

    // Merge guest cart
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
            prepareCartForDto(userCart);
            return userCart;
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

        cartRepository.delete(guestCart);

        prepareCartForDto(userCart);
        return userCart;
    }

    // Update for user
    @Transactional
    public Cart updateCartItemForUser(Long userId, Long productId, int quantity) {
        if (quantity < 1) throw new RuntimeException("Quantity must be at least 1");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        prepareCartForDto(cart);
        return cart;
    }

    // Update for guest
    @Transactional
    public Cart updateCartItemForGuest(String sessionId, Long productId, int quantity) {
        if (quantity < 1) throw new RuntimeException("Quantity must be at least 1");

        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found for session: " + sessionId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        prepareCartForDto(cart);
        return cart;
    }

    // Remove for user
    @Transactional
    public Cart removeItemForUser(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cartItemRepository.delete(item);

        prepareCartForDto(cart);
        return cart;
    }

    // Remove for guest
    @Transactional
    public Cart removeItemForGuest(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found for session: " + sessionId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cartItemRepository.delete(item);

        prepareCartForDto(cart);
        return cart;
    }

    // Clear for user
    @Transactional
    public Cart clearCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));

        List<CartItem> items = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(items);

        prepareCartForDto(cart);
        return cart;
    }

    // Clear for guest
    @Transactional
    public Cart clearCartForGuest(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setSessionId(sessionId);
                    return cartRepository.save(c);
                });

        List<CartItem> items = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(items);

        prepareCartForDto(cart);
        return cart;
    }

    public Cart getCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        prepareCartForDto(cart);
        return cart;
    }

    public Cart getCartForGuest(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setSessionId(sessionId);
                    return cartRepository.save(c);
                });

        prepareCartForDto(cart);
        return cart;
    }

    // Ensure lazy associations are initialized while inside transaction
    private void prepareCartForDto(Cart cart) {
        if (cart == null) return;
        // ensure items loaded
        cart.getCartItems().size();
        // touch fields
        for (CartItem ci : cart.getCartItems()) {
            ci.getCartItemId();
            ci.getProductId();
            ci.getQuantity();
        }
        if (cart.getUser() != null) {
            cart.getUser().getId(); // touch user id only
        }
    }
}
