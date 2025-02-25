package com.myparty.app.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.User;
import jakarta.validation.constraints.NotBlank;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsername(String username);

	boolean existsByUsernameAndUserIdNot(@NotBlank String username, UUID userId);
}
