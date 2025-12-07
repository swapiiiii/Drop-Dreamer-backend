package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUser_Id(Long userId); // ✅ CORRECT & SAFE

    Optional<Cart> findBySessionId(String sessionId);
}

