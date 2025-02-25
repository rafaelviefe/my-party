package com.myparty.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.controller.dto.UpdateUserDto;
import com.myparty.app.entities.User;
import com.myparty.app.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {this.userRepository = userRepository;}

	public Optional<User> findByUsername(String username) {return userRepository.findByUsername(username);}

	public void save(User newUser) {userRepository.save(newUser);}

	public Optional<User> findById(UUID id) {return userRepository.findById(id);}

	public List<User> findAll() {return userRepository.findAll();}

	public void deleteById(UUID userId) {userRepository.deleteById(userId);}

	public void updateUser(UUID userId, UpdateUserDto dto) {
		var user = findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (!user.getUsername().equals(dto.username()) && existsByUsernameAndIdNot(dto.username(), userId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		user.setUsername(dto.username());
		user.setPhoneNumber(dto.phoneNumber());
		user.setStudent(dto.isStudent());
		userRepository.save(user);
	}

	public boolean existsByUsernameAndIdNot(String username, UUID userId) {
		return userRepository.existsByUsernameAndUserIdNot(username, userId);
	}
}
