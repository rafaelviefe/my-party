package com.myparty.app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserDto(
		@NotBlank String username,
		@NotBlank String phoneNumber,
		@NotNull Boolean isStudent
) {
}
