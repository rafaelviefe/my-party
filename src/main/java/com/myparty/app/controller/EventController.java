package com.myparty.app.controller;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.config.SecurityConfig;
import com.myparty.app.controller.dto.EventRevenueDto;
import com.myparty.app.controller.dto.RatingEventDto;
import com.myparty.app.controller.dto.RequestEventDto;
import com.myparty.app.controller.dto.EventResponseDto;
import com.myparty.app.entities.Event;
import com.myparty.app.entities.Ticket;
import com.myparty.app.service.EventService;
import com.myparty.app.service.TicketService;
import com.myparty.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Event", description = "Event management")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class EventController {

	public final EventService eventService;
	public final UserService userService;
	public final TicketService ticketService;

	public EventController(EventService eventService, UserService userService, TicketService ticketService) {
		this.eventService = eventService;
		this.userService = userService;
		this.ticketService = ticketService;
	}

	@Operation(summary = "Create a new event", description = "Method to create a new event")
	@ApiResponse(responseCode = "200", description = "Event created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@ApiResponse(responseCode = "404", description = "Organizer not found")
	@ApiResponse(responseCode = "409", description = "Event already exists")
	@PostMapping("/events")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> newEvent(@RequestBody @Valid RequestEventDto dto, JwtAuthenticationToken token) {

		var organizer = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer not found"));

		eventService.findByTitle(dto.title())
				.ifPresentOrElse(
						(event) -> {throw new ResponseStatusException(HttpStatus.CONFLICT, "Event already exists");},
						() -> {
							var newEvent = new Event();
							newEvent.setOrganizer(organizer);
							newEvent.setTitle(dto.title());
							newEvent.setDescription(dto.description());
							newEvent.setLocation(dto.location());
							newEvent.setDate(dto.date());
							newEvent.setPrice(dto.price());
							newEvent.setCategory(dto.category());
							newEvent.setReviews(0L);
							eventService.save(newEvent);
						}
				);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Get all events", description = "Method to retrieve all events")
	@ApiResponse(responseCode = "200", description = "Events retrieved successfully")
	@GetMapping("/events")
	public ResponseEntity<List<EventResponseDto>> getAllEvents() {
		List<Event> events = eventService.findAll();
		List<EventResponseDto> eventDtos = events.stream()
				.map(eventService::getEventResponse)
				.toList();
		return ResponseEntity.ok(eventDtos);
	}

	@Operation(summary = "Get event by ID", description = "Method to retrieve an event by ID")
	@ApiResponse(responseCode = "200", description = "Event retrieved successfully")
	@GetMapping("/events/{eventId}")
	public ResponseEntity<EventResponseDto> getEvent(@PathVariable Long eventId) {
		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		return ResponseEntity.ok(eventService.getEventResponse(event));
	}

	@Operation(summary = "Get event revenue", description = "Method to retrieve the revenue of an event")
	@ApiResponse(responseCode = "200", description = "Event revenue retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@ApiResponse(responseCode = "404", description = "Event not found")
	@GetMapping("/events/{eventId}/revenue")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<EventRevenueDto> getEventRevenue(@PathVariable Long eventId) {
		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		var revenue = ticketService.calculateRevenueByEvent(eventId);

		return ResponseEntity.ok(new EventRevenueDto(eventId, revenue != null ? revenue : 0.0));
	}

	@Operation(summary = "Get event rating statistics", description = "Method to retrieve the rating statistics of all events")
	@ApiResponse(responseCode = "200", description = "Event rating statistics retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@GetMapping("/events/rating-statistics")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<DoubleSummaryStatistics> getEventRatingStatistics() {
		List<Event> events = eventService.findAll();
		var statistics = Event.calculateRatingStatistics(events);
		return ResponseEntity.ok(statistics);
	}

	@Operation(summary = "Update an event", description = "Method to update an event by ID")
	@ApiResponse(responseCode = "200", description = "Event updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Insufficient permissions")
	@ApiResponse(responseCode = "404", description = "Event not found")
	@ApiResponse(responseCode = "409", description = "Event title already exists")
	@PutMapping("/events/{eventId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> updateEvent(@PathVariable Long eventId, @RequestBody @Valid RequestEventDto dto) {

		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		if (!event.getTitle().equals(dto.title()) && eventService.existsByTitleAndIdNot(dto.title(), eventId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Event title already exists");
		}

		event.setTitle(dto.title());
		event.setDescription(dto.description());
		event.setLocation(dto.location());
		event.setDate(dto.date());
		event.setPrice(dto.price());
		event.setCategory(dto.category());
		eventService.save(event);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Update event organizer", description = "Method to update the organizer of an event")
	@ApiResponse(responseCode = "200", description = "Event organizer updated successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "Event or organizer not found")
	@ApiResponse(responseCode = "403", description = "Only the event organizer can update the organizer")
	@PatchMapping("/events/{eventId}/{organizerId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> updateEventOrganizer(@PathVariable Long eventId, @PathVariable UUID organizerId, JwtAuthenticationToken token) {

		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		if (!event.getOrganizer().getUserId().equals(UUID.fromString(token.getName()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event organizer can update the organizer");
		}

		var organizer = userService.findById(organizerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer not found"));

		event.setOrganizer(organizer);
		eventService.save(event);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Rate an event", description = "Method to rate an event by ID")
	@ApiResponse(responseCode = "200", description = "Event rated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "404", description = "Event or user not found")
	@ApiResponse(responseCode = "412", description = "Precondition failed - rating or date unexpected")
	@Transactional
	@PatchMapping("/events/{eventId}/rating")
	public ResponseEntity<Void> rateEvent(@PathVariable Long eventId, @RequestBody @Valid RatingEventDto dto, JwtAuthenticationToken token) {

		var rating  = dto.rating();
		if (rating < 0 || rating > 5 || (rating * 10) % 5 != 0) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED , "Rating must be between 0 and 5 and multiple of 0.5");
		}

		var user = userService.findById(UUID.fromString(token.getName()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		if (event.getDate().isAfter(Instant.now())) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Event has not happened yet");
		}

		var ticket = ticketService.findByUserAndEventAndStatus(user, event, Ticket.Status.APPROVED)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not have a valid ticket for this event"));

		var eventRating = event.getRating() != null ? event.getRating() : 0;
		if (ticket.getRating() != null) {
			event.setRating((eventRating * event.getReviews() - ticket.getRating() + rating) / event.getReviews());
		} else {
			event.setRating((eventRating * event.getReviews() + rating) / (event.getReviews() + 1));
			event.setReviews(event.getReviews() + 1);
		}

		ticket.setRating(rating);
		ticketService.save(ticket);

		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Delete an event", description = "Method to delete an event by ID")
	@ApiResponse(responseCode = "200", description = "Event deleted successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	@ApiResponse(responseCode = "403", description = "Only the event organizer can delete the event")
	@ApiResponse(responseCode = "404", description = "Event not found")
	@Transactional
	@DeleteMapping("/events/{eventId}")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, JwtAuthenticationToken token) {

		var event = eventService.findById(eventId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		if (!event.getOrganizer().getUserId().equals(UUID.fromString(token.getName()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event organizer can delete the event");
		}

		eventService.deleteById(event);

		return ResponseEntity.ok().build();
	}

}
