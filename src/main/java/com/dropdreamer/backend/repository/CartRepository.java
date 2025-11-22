package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Get cart for logged-in user by User object
    Optional<Cart> findByUser(User user);

    // Convenience method: Get cart for logged-in user by userId
    default Optional<Cart> findByUserId(Long userId) {
        return findByUser(new User(userId));
    }

    // Get cart for guest (session)
    Optional<Cart> findBySessionId(String sessionId);
}
