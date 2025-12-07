package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.Cart;
import com.dropdreamer.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
    List<CartItem> findByCart(Cart cart);
}
