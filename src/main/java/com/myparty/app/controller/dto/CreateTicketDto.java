package com.myparty.app.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketDto(
		@NotBlank String eventTitle
) {
}
