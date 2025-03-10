package com.myparty.app.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.controller.dto.CreateUserDto;
import com.myparty.app.controller.dto.UpdatePasswordDto;
import com.myparty.app.controller.dto.UpdateUserDto;
import com.myparty.app.entities.User;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
public class UserController {

	private final UserService userService;
	private final TicketService ticketService;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserController(UserService userService, TicketService ticketService, BCryptPasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.ticketService = ticketService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	@PostMapping("/users")
	public ResponseEntity<Void> newUser(@RequestBody @Valid CreateUserDto dto) {

		if (userService.findByUsername(dto.username()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
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

	@GetMapping("/users")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<List<User>> getUsers() {
		return ResponseEntity.ok(userService.findAll());
	}

	@GetMapping("/users/{userId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<User> getUser(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
	}

	@PutMapping("/users")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UpdateUserDto dto, JwtAuthenticationToken token) {
		var userId = UUID.fromString(token.getName());
		userService.updateUser(userId, dto);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/users/{userId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> updateOtherUser(@PathVariable UUID userId, @RequestBody @Valid UpdateUserDto dto) {
		userService.updateUser(userId, dto);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/users")
	public ResponseEntity<Void> updatePassword(@RequestBody @Valid UpdatePasswordDto dto, JwtAuthenticationToken token) {
		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (user.passwordMatches(dto.oldPassword(), passwordEncoder)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The old password is incorrect");
		}

		user.setPassword(passwordEncoder.encode(dto.newPassword()));
		userService.save(user);

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/users/{userId}/{role}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> updateUserRole(@PathVariable UUID userId, @PathVariable String role, JwtAuthenticationToken token) {

		var user = userService.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		User.Role newRole;
		try {newRole = User.Role.fromString(role);}
		catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + role);
		}

		validateRoleChange(token, user, newRole);

		user.setRole(newRole);
		userService.save(user);

		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/users/{userId}")
	@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
		var user = userService.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (!ticketService.findByUser(user).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User has tickets");
		}

		userService.deleteById(userId);
		return ResponseEntity.ok().build();
	}

	private void validateRoleChange(JwtAuthenticationToken token, User user, User.Role newRole) {
		if (user.getRole() == newRole) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has this role");
		}

		var requester = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Requester not found"));

		if (user.getRole() == User.Role.ADMIN && requester.getRole() != User.Role.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can change the role of other admins");
		}
	}

}
