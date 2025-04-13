package com.blog.repository;

import com.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByGoogleId(String googleId);

    boolean existsByUsername(String username);
}
