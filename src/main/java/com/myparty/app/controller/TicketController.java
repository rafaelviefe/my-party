package com.myparty.app.controller;

import java.util.DoubleSummaryStatistics;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.controller.dto.CreateTicketDto;
import com.myparty.app.entities.Ticket;
import com.myparty.app.service.EventService;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import jakarta.validation.Valid;

@RestController
public class TicketController {

	private final TicketService ticketService;
	private final UserService userService;
	private final EventService eventService;

	public TicketController(TicketService ticketService, UserService userService, EventService eventService) {
		this.ticketService = ticketService;
		this.userService = userService;
		this.eventService = eventService;
	}

	@PostMapping("/tickets")
	public ResponseEntity<Void> newTicket(@RequestBody @Valid CreateTicketDto dto, JwtAuthenticationToken token) {

		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		var event = eventService.findByTitle(dto.eventTitle())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		ticketService.createTicket(user, event);

		// TODO: program a service that will approve or reject the request in 5 minutes

		return ResponseEntity.ok().build();
	}

	@GetMapping("/tickets/rating-statistics")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<DoubleSummaryStatistics> getRatingStatistics() {
		var tickets = ticketService.findAll();
		var statistics = Ticket.calculateRatingStatistics(tickets);
		return ResponseEntity.ok(statistics);
	}

}
