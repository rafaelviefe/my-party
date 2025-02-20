package com.myparty.app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDto(
		@NotBlank String username,
		@NotBlank String password,
		@NotBlank String phoneNumber,
		@NotNull Boolean isStudent
) {
}
