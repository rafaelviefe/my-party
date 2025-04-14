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
import com.myparty.app.config.SecurityConfig;
import com.myparty.app.controller.dto.CreateTicketDto;
import com.myparty.app.controller.dto.TicketResponseDto;
import com.myparty.app.entities.Ticket;
import com.myparty.app.service.EventService;
import com.myparty.app.service.PaymentProcessorService;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Ticket", description = "Ticket management")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
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

	@Operation(summary = "Create a new ticket", description = "Method to create a new ticket")
	@ApiResponse(responseCode = "200", description = "Ticket created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input or event has already happened")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "User or event not found")
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

		paymentProcessorService.processPayment(ticket, Ticket.Status.PENDING);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Get all tickets", description = "Method to retrieve all tickets")
	@ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@GetMapping("/tickets")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<List<TicketResponseDto>> getTickets() {
		List<Ticket> tickets = ticketService.findAll();
		List<TicketResponseDto> ticketDtos = tickets.stream()
				.map(TicketResponseDto::fromEntity)
				.toList();
		return ResponseEntity.ok(ticketDtos);
	}

	@Operation(summary = "Get tickets of the logged-in user", description = "Retrieves all tickets purchased by the currently authenticated user")
	@ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "User not found")
	@GetMapping("/tickets/me")
	public ResponseEntity<List<TicketResponseDto>> getMyTickets(JwtAuthenticationToken token) {
		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		List<Ticket> tickets = ticketService.findByUser(user);
		List<TicketResponseDto> ticketDtos = tickets.stream().map(TicketResponseDto::fromEntity).toList();

		return ResponseEntity.ok(ticketDtos);
	}

	@Operation(summary = "Get ticket rating statistics", description = "Retrieves rating statistics for all tickets")
	@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@GetMapping("/tickets/rating-statistics")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<DoubleSummaryStatistics> getRatingStatistics() {
		var tickets = ticketService.findAll();
		var statistics = Ticket.calculateRatingStatistics(tickets);
		return ResponseEntity.ok(statistics);
	}

	@Operation(summary = "Retry payment for a ticket", description = "Retries payment for a rejected ticket")
	@ApiResponse(responseCode = "200", description = "Payment retried successfully")
	@ApiResponse(responseCode = "400", description = "Ticket is not rejected")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "You are not allowed to retry this ticket")
	@ApiResponse(responseCode = "404", description = "User or ticket not found")
	@PatchMapping("/tickets/{ticketId}/retry")
	public ResponseEntity<Void> retryPayment(@PathVariable Long ticketId, JwtAuthenticationToken token) {
		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		var ticket = ticketService.findById(ticketId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

		if (!ticket.getUser().equals(user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to retry this ticket");
		}

		if (ticket.getStatus() != Ticket.Status.REJECTED) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket is not rejected");
		}

		paymentProcessorService.processPayment(ticket, Ticket.Status.REJECTED);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Cancel a ticket", description = "Cancels a ticket by changing its status to rejected")
	@ApiResponse(responseCode = "200", description = "Ticket cancelled successfully")
	@ApiResponse(responseCode = "400", description = "Ticket already rejected or event has already happened")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "You are not allowed to cancel this ticket")
	@ApiResponse(responseCode = "404", description = "User or ticket not found")
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
