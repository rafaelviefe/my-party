package com.myparty.app.entities;

import jakarta.persistence.*;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.myparty.app.controller.dto.LoginRequestDto;

@Entity
@Table(name = "tb_users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID userId;

	@Column(unique = true)
	private String username;

	private String password;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "is_student")
	private Boolean isStudent;

	public boolean isLoginCorrect(LoginRequestDto loginRequestDto, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(loginRequestDto.password(), this.password);
	}

	public enum Role {
		ADMIN("Admin"),
		ORGANIZER("Organizer"),
		PARTICIPANT("Participant");

		private final String displayName;

		Role(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public static Role fromString(String role) {
			for (Role r : Role.values()) {
				if (r.name().equalsIgnoreCase(role)) {
					return r;
				}
			}
			throw new IllegalArgumentException("Invalid role: " + role);
		}
	}

	public User() {
	}

	@Override public String toString() {
		return "User{" +
				"userId=" + userId +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", role=" + role +
				", isStudent=" + isStudent +
				'}';
	}

	public User(UUID userId, String username, String password, String phoneNumber, Role role, Boolean isStudent) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.role = role;
		this.isStudent = isStudent;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Boolean getStudent() {
		return isStudent;
	}

	public void setStudent(Boolean student) {
		isStudent = student;
	}
}
