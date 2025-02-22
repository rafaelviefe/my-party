package com.myparty.app.controller.dto;

import jakarta.validation.constraints.NotNull;

public record CreateTicketDto(
		@NotNull Long eventId
) {
}
