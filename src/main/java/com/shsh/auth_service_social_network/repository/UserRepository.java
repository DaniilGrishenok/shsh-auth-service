package com.shsh.auth_service_social_network.repository;

import com.shsh.auth_service_social_network.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByEmail(String email);

    boolean existsByEmail(String email);
}
