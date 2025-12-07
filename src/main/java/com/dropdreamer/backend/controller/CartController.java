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
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public CartController(CartService cartService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // -------------------- USER CART --------------------

    @PostMapping("/user/add")
    public ResponseEntity<CartResponse> addToUserCart(HttpServletRequest request,
                                                      @RequestBody AddToCartRequest body) {
        if (body.getProductId() == null) return ResponseEntity.badRequest().build();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        int quantity = (body.getQuantity() == null || body.getQuantity() <= 0) ? 1 : body.getQuantity();
        Cart cart = cartService.addToCartForUser(user.getId(), body.getProductId(), quantity);

        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PutMapping("/user/update")
    public ResponseEntity<CartResponse> updateUserCart(HttpServletRequest request,
                                                       @RequestBody UpdateCartRequest body) {
        if (body.getProductId() == null || body.getQuantity() == null || body.getQuantity() < 0)
            return ResponseEntity.badRequest().build();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.updateCartItemForUser(user.getId(), body.getProductId(), body.getQuantity());
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @DeleteMapping("/user/remove")
    public ResponseEntity<?> removeUserCartItem(HttpServletRequest request,
                                                @RequestParam Long productId) {
        if (productId == null) return ResponseEntity.badRequest().build();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.removeItemForUser(user.getId(), productId);
        return cart == null ? ResponseEntity.ok("Cart deleted successfully")
                : ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PatchMapping("/user/decrement")
    public ResponseEntity<?> decrementUserCartItem(HttpServletRequest request,
                                                   @RequestParam Long productId) {
        if (productId == null) return ResponseEntity.badRequest().build();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.decrementItemForUser(user.getId(), productId);
        return cart == null ? ResponseEntity.ok("Cart deleted successfully")
                : ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @DeleteMapping("/user/clear")
    public ResponseEntity<?> clearUserCart(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        cartService.clearCartForUser(user.getId());
        return ResponseEntity.ok("Cart cleared and deleted successfully");
    }

    @GetMapping("/user/get")
    public ResponseEntity<CartResponse> getUserCart(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getCartForUser(user.getId());
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PostMapping("/user/merge")
    public ResponseEntity<CartResponse> mergeGuestCartWithUser(HttpServletRequest request,
                                                               @RequestParam String sessionId) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).build();

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.mergeGuestCartWithUser(sessionId, user.getId());
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    // -------------------- GUEST CART --------------------

    @PostMapping("/guest/add")
    public ResponseEntity<CartResponse> addToGuestCart(@RequestParam String sessionId,
                                                       @RequestBody AddToCartRequest body) {
        if (body.getProductId() == null || sessionId.isBlank()) return ResponseEntity.badRequest().build();

        int quantity = (body.getQuantity() == null || body.getQuantity() <= 0) ? 1 : body.getQuantity();
        Cart cart = cartService.addToCartForGuest(sessionId, body.getProductId(), quantity);

        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PutMapping("/guest/update")
    public ResponseEntity<CartResponse> updateGuestCart(@RequestParam String sessionId,
                                                        @RequestBody UpdateCartRequest body) {
        if (body.getProductId() == null || body.getQuantity() == null || body.getQuantity() < 0
                || sessionId.isBlank()) return ResponseEntity.badRequest().build();

        Cart cart = cartService.updateCartItemForGuest(sessionId, body.getProductId(), body.getQuantity());
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @DeleteMapping("/guest/remove")
    public ResponseEntity<?> removeGuestCartItem(@RequestParam String sessionId,
                                                 @RequestParam Long productId) {
        if (productId == null || sessionId.isBlank()) return ResponseEntity.badRequest().build();

        Cart cart = cartService.removeItemForGuest(sessionId, productId);
        return cart == null ? ResponseEntity.ok("Cart deleted successfully")
                : ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PatchMapping("/guest/decrement")
    public ResponseEntity<?> decrementGuestCartItem(@RequestParam String sessionId,
                                                    @RequestParam Long productId) {
        if (productId == null || sessionId.isBlank()) return ResponseEntity.badRequest().build();

        Cart cart = cartService.decrementItemForGuest(sessionId, productId);
        return cart == null ? ResponseEntity.ok("Cart deleted successfully")
                : ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @DeleteMapping("/guest/clear")
    public ResponseEntity<?> clearGuestCart(@RequestParam String sessionId) {
        if (sessionId.isBlank()) return ResponseEntity.badRequest().build();

        cartService.clearCartForGuest(sessionId);
        return ResponseEntity.ok("Guest cart cleared and deleted successfully");
    }

    @GetMapping("/guest/get")
    public ResponseEntity<CartResponse> getGuestCart(@RequestParam String sessionId) {
        if (sessionId.isBlank()) return ResponseEntity.badRequest().build();

        Cart cart = cartService.getCartForGuest(sessionId);
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }
}
