package com.myparty.app.controller.dto;

import java.time.Instant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestEventDto(
		@NotBlank String title,
		@NotBlank String description,
		@NotBlank String location,
		@NotNull Instant date,
		@NotNull Double price,
		@NotBlank String category
) {
}
