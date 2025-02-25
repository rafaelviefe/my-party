package com.myparty.app.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.myparty.app.controller.dto.EventResponseDto;
import com.myparty.app.entities.Event;
import com.myparty.app.repository.EventRepository;

@Service
public class EventService {

	private final EventRepository eventRepository;

	public EventService(EventRepository eventRepository) {this.eventRepository = eventRepository;}

	public void save(Event event) {eventRepository.save(event);}

	public Optional<Event> findById(Long id) {return eventRepository.findById(id);}

	public Optional<Event> findByTitle(String title) {return eventRepository.findByTitle(title);}

	public List<Event> findAll() {return eventRepository.findAll();}

	public void deleteById(Long eventId) {eventRepository.deleteById(eventId);}

	public boolean existsByTitleAndIdNot(String title, Long eventId) {return eventRepository.existsByTitleAndEventIdNot(title, eventId);}

	public EventResponseDto getEventResponse(Event event) {
		Long ticketCount = eventRepository.countTicketsByEventId(event.getEventId());
		return new EventResponseDto(
				event.getEventId(),
				event.getTitle(),
				event.getDescription(),
				event.getLocation(),
				event.getDate(),
				event.getPrice(),
				event.getCategory(),
				event.getRating(),
				event.getReviews(),
				event.getOrganizer().getUsername(),
				ticketCount
		);
	}
}
