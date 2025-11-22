package com.dropdreamer.backend.controller;

import com.dropdreamer.backend.dto.AddToCartRequest;
import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.User;
import com.dropdreamer.backend.repository.UserRepository;
import com.dropdreamer.backend.service.CartService;
import com.dropdreamer.backend.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

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
    public ResponseEntity<Cart> addToCart(
            HttpServletRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestBody AddToCartRequest body) {

        // Check if JWT is present
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            Cart cart = cartService.addToCartForUser(user.getId(), body.getProductId(), body.getQuantity());
            return ResponseEntity.ok(cart);
        }

        // Guest cart (no JWT)
        if (sessionId != null && !sessionId.isEmpty()) {
            Cart cart = cartService.addToCartForGuest(sessionId, body.getProductId(), body.getQuantity());
            return ResponseEntity.ok(cart);
        }

        return ResponseEntity.badRequest().build();
    }

    // Merge guest cart after login
    @PostMapping("/merge")
    public ResponseEntity<Cart> mergeGuestCart(
            HttpServletRequest request,
            @RequestParam String sessionId) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            Cart cart = cartService.mergeGuestCartWithUser(sessionId, user.getId());
            return ResponseEntity.ok(cart);
        }

        return ResponseEntity.status(401).build();
    }
}
