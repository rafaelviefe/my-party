package com.myparty.app.controller;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.controller.dto.CreateTicketDto;
import com.myparty.app.controller.dto.TicketResponseDto;
import com.myparty.app.entities.Ticket;
import com.myparty.app.service.EventService;
import com.myparty.app.service.PaymentProcessorService;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import jakarta.validation.Valid;

@RestController
public class TicketController {

	private final TicketService ticketService;
	private final UserService userService;
	private final EventService eventService;
	private final PaymentProcessorService paymentProcessorService;

	public TicketController(
			TicketService ticketService,
			UserService userService,
			EventService eventService,
			PaymentProcessorService paymentProcessorService) {
		this.ticketService = ticketService;
		this.userService = userService;
		this.eventService = eventService;
		this.paymentProcessorService = paymentProcessorService;
	}

	@PostMapping("/tickets")
	public ResponseEntity<Void> newTicket(@RequestBody @Valid CreateTicketDto dto, JwtAuthenticationToken token) {

		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		var event = eventService.findByTitle(dto.eventTitle())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		if (event.getDate().isBefore(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event has already happened");
		}

		var ticket = ticketService.createTicket(user, event);

		paymentProcessorService.processPayment(ticket);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/tickets")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<List<TicketResponseDto>> getTickets() {
		List<Ticket> tickets = ticketService.findAll();
		List<TicketResponseDto> ticketDtos = tickets.stream()
				.map(TicketResponseDto::fromEntity)
				.toList();
		return ResponseEntity.ok(ticketDtos);
	}

	@GetMapping("/tickets/me")
	public ResponseEntity<List<TicketResponseDto>> getMyTickets(JwtAuthenticationToken token) {
		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		List<Ticket> tickets = ticketService.findByUser(user);
		List<TicketResponseDto> ticketDtos = tickets.stream().map(TicketResponseDto::fromEntity).toList();

		return ResponseEntity.ok(ticketDtos);
	}

	@GetMapping("/tickets/rating-statistics")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<DoubleSummaryStatistics> getRatingStatistics() {
		var tickets = ticketService.findAll();
		var statistics = Ticket.calculateRatingStatistics(tickets);
		return ResponseEntity.ok(statistics);
	}

	@PatchMapping("/tickets/{ticketId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> cancelTicket(@PathVariable Long ticketId, JwtAuthenticationToken token) {

		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		var ticket = ticketService.findById(ticketId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

		if (!ticket.getUser().equals(user) && !ticket.getEvent().getOrganizer().equals(user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel this ticket");
		}

		if (ticket.getStatus() == Ticket.Status.REJECTED) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket already rejected");
		}

		if (ticket.getEvent().getDate().isBefore(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event has already happened");
		}

		ticketService.updateTicketStatus(ticket, Ticket.Status.REJECTED);

		return ResponseEntity.ok().build();
	}

}
