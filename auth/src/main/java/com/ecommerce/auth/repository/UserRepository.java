package com.ecommerce.auth.repository;

import com.ecommerce.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username);
    boolean existsByUsername(String username);
}
