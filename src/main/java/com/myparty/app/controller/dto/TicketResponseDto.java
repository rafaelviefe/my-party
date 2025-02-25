package com.myparty.app.controller.dto;

import java.time.Instant;
import com.myparty.app.entities.Ticket;

public record TicketResponseDto(
		Long ticketId,
		String eventTitle,
		Instant eventDate,
		String status,
		Double rating
) {
	public static TicketResponseDto fromEntity(Ticket ticket) {
		return new TicketResponseDto(
				ticket.getTicketId(),
				ticket.getEvent().getTitle(),
				ticket.getEvent().getDate(),
				ticket.getStatus().name(),
				ticket.getRating()
		);
	}
}
