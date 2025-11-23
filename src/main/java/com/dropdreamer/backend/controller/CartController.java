package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.dto.AddToCartRequest;
import com.dropdreamer.backend.dto.CartResponse;
import com.dropdreamer.backend.dto.UpdateCartRequest;
import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.UserRepository;
import com.dropdreamer.backend.service.CartService;
import com.dropdreamer.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public CartController(CartService cartService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // Add to cart (guest or logged-in)
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestBody AddToCartRequest body) {

        String authHeader = request.getHeader("Authorization");
        Cart cart;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            cart = cartService.addToCartForUser(user.getId(), body.getProductId(),
                    body.getQuantity() == null ? 1 : body.getQuantity());
        } else if (sessionId != null && !sessionId.isEmpty()) {
            cart = cartService.addToCartForGuest(sessionId, body.getProductId(),
                    body.getQuantity() == null ? 1 : body.getQuantity());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    // Merge guest cart after login
    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeGuestCart(
            HttpServletRequest request,
            @RequestParam String sessionId) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            Cart cart = cartService.mergeGuestCartWithUser(sessionId, user.getId());
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        return ResponseEntity.status(401).build();
    }

    // Update quantity of a cart item
    @PostMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestBody UpdateCartRequest body) {

        String authHeader = request.getHeader("Authorization");

        Cart cart;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            cart = cartService.updateCartItemForUser(user.getId(), body.getProductId(), body.getQuantity());
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        if (sessionId != null && !sessionId.isEmpty()) {
            cart = cartService.updateCartItemForGuest(sessionId, body.getProductId(), body.getQuantity());
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        return ResponseEntity.badRequest().build();
    }

    // Remove item from cart (logged-in / guest)
    @DeleteMapping("/remove")
    public ResponseEntity<CartResponse> removeCartItem(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestParam Long productId) {

        String authHeader = request.getHeader("Authorization");
        Cart cart;

        // Logged-in user
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            cart = cartService.removeItemForUser(user.getId(), productId);
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        // Guest user
        if (sessionId != null && !sessionId.isEmpty()) {
            cart = cartService.removeItemForGuest(sessionId, productId);
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        return ResponseEntity.badRequest().build();
    }

    // Clear entire cart (logged-in / guest)
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId) {

        String authHeader = request.getHeader("Authorization");
        Cart cart;

        // Logged-in user
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            cart = cartService.clearCartForUser(user.getId());
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        // Guest user
        if (sessionId != null && !sessionId.isEmpty()) {
            cart = cartService.clearCartForGuest(sessionId);
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        return ResponseEntity.badRequest().build();
    }

    // Get cart (logged-in / guest)
    @GetMapping("/get")
    public ResponseEntity<CartResponse> getCart(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId
    ) {

        String authHeader = request.getHeader("Authorization");
        Cart cart;

        // Logged-in user
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            cart = cartService.getCartForUser(user.getId());
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        // Guest user
        if (sessionId != null && !sessionId.isEmpty()) {
            cart = cartService.getCartForGuest(sessionId);
            return ResponseEntity.ok(CartResponse.fromCart(cart));
        }

        return ResponseEntity.badRequest().body(null);
    }
}
