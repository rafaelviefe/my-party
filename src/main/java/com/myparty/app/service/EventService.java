package com.myparty.app.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.myparty.app.entities.Event;
import com.myparty.app.repository.EventRepository;

@Service
public class EventService {

	private final EventRepository eventRepository;

	public EventService(EventRepository eventRepository) {this.eventRepository = eventRepository;}

	public Optional<Event> findById(Long id) {return eventRepository.findById(id);}

}
