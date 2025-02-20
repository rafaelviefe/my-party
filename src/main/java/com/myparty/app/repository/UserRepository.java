package com.myparty.app.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.myparty.app.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	User findByUsername(String username);

}
