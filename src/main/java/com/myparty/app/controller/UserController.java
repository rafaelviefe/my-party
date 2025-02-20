package com.myparty.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.controller.dto.CreateUserDto;
import com.myparty.app.entities.User;
import com.myparty.app.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
public class UserController {

	private final UserService userService;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	@PostMapping("/users")
	public ResponseEntity<Void> newUser(@RequestBody @Valid CreateUserDto dto){

		var userFromDb = userService.findByUsername(dto.username());

		if (userFromDb.isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
		}

		var newUser = new User();
		newUser.setUsername(dto.username());
		newUser.setPassword(passwordEncoder.encode(dto.password()));
		newUser.setRole(User.Role.PARTICIPANT);
		newUser.setPhoneNumber(dto.phoneNumber());
		newUser.setStudent(dto.isStudent());

		userService.save(newUser);

		return ResponseEntity.ok().build();
	}

}
