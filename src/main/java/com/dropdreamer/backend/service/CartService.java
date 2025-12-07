package com.dropdreamer.backend.service;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.CartItem;
import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.CartItemRepository;
import com.dropdreamer.backend.repository.CartRepository;
import com.dropdreamer.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // -------------------- ADD TO CART --------------------
    @Transactional
    public Cart addToCartForGuest(String sessionId, Long productId, int quantity) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(productId);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
        prepareCartForDto(cart);
        return cart;
    }

    @Transactional
    public Cart addToCartForUser(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(productId);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
        prepareCartForDto(cart);
        return cart;
    }

    // -------------------- UPDATE CART ITEM --------------------
    @Transactional
    public Cart updateCartItemForGuest(String sessionId, Long productId, int quantity) {
        if (quantity <= 0) return removeItemForGuest(sessionId, productId);

        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        prepareCartForDto(cart);
        return cart;
    }

    @Transactional
    public Cart updateCartItemForUser(Long userId, Long productId, int quantity) {
        if (quantity <= 0) return removeItemForUser(userId, productId);

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        prepareCartForDto(cart);
        return cart;
    }

    // -------------------- REMOVE ITEM --------------------
    @Transactional
    public Cart removeItemForGuest(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        cart.removeCartItem(item);
        cartItemRepository.delete(item);

        if (cart.getCartItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemForUser(Long userId, Long productId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        cart.removeCartItem(item);
        cartItemRepository.delete(item);

        if (cart.getCartItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        }

        return cartRepository.save(cart);
    }

    // -------------------- DECREMENT ITEM --------------------
    @Transactional
    public Cart decrementItemForGuest(String sessionId, Long productId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            return removeItemForGuest(sessionId, productId);
        }

        prepareCartForDto(cart);
        return cart;
    }

    @Transactional
    public Cart decrementItemForUser(Long userId, Long productId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            return removeItemForUser(userId, productId);
        }

        prepareCartForDto(cart);
        return cart;
    }

    // -------------------- CLEAR CART --------------------
    @Transactional
    public void clearCartForGuest(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItems());
        cartRepository.delete(cart);
    }

    @Transactional
    public void clearCartForUser(Long userId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteAll(cart.getCartItems());
        cartRepository.delete(cart);
    }

    // -------------------- GET CART --------------------
    public Cart getCartForGuest(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(sessionId);
                    return cartRepository.save(newCart);
                });

        prepareCartForDto(cart);
        return cart;
    }

    public Cart getCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        prepareCartForDto(cart);
        return cart;
    }

    // -------------------- MERGE GUEST CART --------------------
    @Transactional
    public Cart mergeGuestCartWithUser(String sessionId, Long userId) {
        Cart guestCart = cartRepository.findBySessionId(sessionId).orElse(null);
        if (guestCart == null) return getCartForUser(userId);

        Cart userCart = cartRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(userRepository.findById(userId).orElseThrow());
                    return cartRepository.save(c);
                });

        for (CartItem item : guestCart.getCartItems()) {
            CartItem existing = cartItemRepository.findByCartAndProductId(userCart, item.getProductId())
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                cartItemRepository.save(existing);
            } else {
                item.setCart(userCart);
                cartItemRepository.save(item);
            }
        }

        cartRepository.delete(guestCart);
        prepareCartForDto(userCart);
        return userCart;
    }

    // -------------------- HELPER --------------------
    private void prepareCartForDto(Cart cart) {
        if (cart == null) return;

        cart.getCartItems().size();
        for (CartItem ci : cart.getCartItems()) {
            ci.getCartItemId();
            ci.getProductId();
            ci.getQuantity();
        }

        if (cart.getUser() != null) {
            cart.getUser().getId();
        }
    }
}
