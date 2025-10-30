package com.dropdreamer.backend.repository;

import com.dropdreamer.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Find user by email (for login and signup checks)
    Optional<User> findByEmail(String email);

    // ✅ Check if user already exists
    boolean existsByEmail(String email);
}
