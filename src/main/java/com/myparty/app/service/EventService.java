package com.myparty.app.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
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
}
