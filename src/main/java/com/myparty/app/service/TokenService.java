package com.myparty.app.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.myparty.app.entities.User;
import com.myparty.app.repository.UserRepository;

@Service
public class TokenService {

	private final UserRepository userRepository;

	public TokenService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

}
