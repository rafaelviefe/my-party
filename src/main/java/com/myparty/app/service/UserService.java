package com.myparty.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
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
}
