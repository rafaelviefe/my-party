package com.myparty.app.controller;

import java.util.List;
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
import com.myparty.app.controller.dto.CreateEventDto;
import com.myparty.app.controller.dto.EventResponseDto;
import com.myparty.app.entities.Event;
import com.myparty.app.service.EventService;
import com.myparty.app.service.UserService;
import jakarta.validation.Valid;

@RestController
public class EventController {

	public final EventService eventService;
	public final UserService userService;

	public EventController(EventService eventService, UserService userService) {
		this.eventService = eventService;
		this.userService = userService;
	}

	@PostMapping("/events")
	@PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_ORGANIZER')")
	public ResponseEntity<Void> newEvent(@RequestBody @Valid CreateEventDto dto, JwtAuthenticationToken token) {

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

		// TODO: send an SMS to the participant users using the Twilio API 2 days before the event (if ticket accepted)

		return ResponseEntity.ok().build();
	}

	@GetMapping("/events")
	public ResponseEntity<List<EventResponseDto>> getEvents() {
		List<EventResponseDto> events = eventService.findAll().stream()
				.map(event -> new EventResponseDto(
						event.getEventId(),
						event.getTitle(),
						event.getDescription(),
						event.getLocation(),
						event.getDate(),
						event.getPrice(),
						event.getCategory(),
						event.getRating(),
						event.getReviews(),
						event.getOrganizer().getUsername()
				))
				.toList();

		return ResponseEntity.ok(events);
	}

}
