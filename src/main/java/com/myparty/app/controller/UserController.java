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
import com.myparty.app.config.SecurityConfig;
import com.myparty.app.controller.dto.CreateUserDto;
import com.myparty.app.controller.dto.UpdatePasswordDto;
import com.myparty.app.controller.dto.UpdateUserDto;
import com.myparty.app.entities.User;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@Tag(name = "User", description = "User management")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class UserController {

	private final UserService userService;
	private final TicketService ticketService;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserController(UserService userService, TicketService ticketService, BCryptPasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.ticketService = ticketService;
		this.passwordEncoder = passwordEncoder;
	}

	@Operation(summary = "Create a new user", description = "Method to create a new user")
	@ApiResponse(responseCode = "200", description = "User created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "409", description = "User already exists")
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

	@Operation(summary = "Get all users", description = "Method to retrieve all users")
	@ApiResponse(responseCode = "200", description = "Users retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@GetMapping("/users")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<List<User>> getUsers() {
		return ResponseEntity.ok(userService.findAll());
	}

	@Operation(summary = "Get user by ID", description = "Method to retrieve a user by ID")
	@ApiResponse(responseCode = "200", description = "User retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@ApiResponse(responseCode = "404", description = "User not found")
	@GetMapping("/users/{userId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<User> getUser(@PathVariable UUID userId) {
		return ResponseEntity.ok(userService.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
	}

	@Operation(summary = "Update the logged-in user", description = "Method to update the currently authenticated user")
	@ApiResponse(responseCode = "200", description = "User updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@PutMapping("/users")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UpdateUserDto dto, JwtAuthenticationToken token) {
		var userId = UUID.fromString(token.getName());
		userService.updateUser(userId, dto);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update another user", description = "Method to update another user")
	@ApiResponse(responseCode = "200", description = "User updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@PutMapping("/users/{userId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> updateOtherUser(@PathVariable UUID userId, @RequestBody @Valid UpdateUserDto dto) {
		userService.updateUser(userId, dto);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update password", description = "Method to update the password of the logged-in user")
	@ApiResponse(responseCode = "204", description = "Password updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions or old password is incorrect")
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

	@Operation(summary = "Update user role", description = "Method to update the role of a user")
	@ApiResponse(responseCode = "200", description = "User role updated successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized or requester not found")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions or role change not allowed")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "409", description = "User already has this role")
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

	@Operation(summary = "Delete a user", description = "Method to delete a user by ID")
	@ApiResponse(responseCode = "200", description = "User deleted successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@ApiResponse(responseCode = "404", description = "User not found")
	@ApiResponse(responseCode = "409", description = "User has tickets")
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
