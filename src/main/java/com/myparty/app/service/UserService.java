package com.myparty.app.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.myparty.app.entities.User;
import com.myparty.app.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {this.userRepository = userRepository;}

	public Optional<User> findByUsername(String username) {return userRepository.findByUsername(username);}

	public void save(User newUser) {userRepository.save(newUser);}
}
