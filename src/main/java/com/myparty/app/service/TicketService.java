package com.myparty.app.service;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.myparty.app.entities.Event;
import com.myparty.app.entities.Ticket;
import com.myparty.app.entities.User;
import com.myparty.app.repository.TicketRepository;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;

	public TicketService(TicketRepository ticketRepository) {this.ticketRepository = ticketRepository;}

	public void save(Ticket ticket) {ticketRepository.save(ticket);}

	public List<Ticket> findAll() {return ticketRepository.findAll();}

	public Ticket createTicket(User user, Event event) {

		if (ticketRepository.existsByUserAndEventAndStatusNot(user, event, Ticket.Status.REJECTED)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The User already have a ticket for this event.");
		}

		var newTicket = new Ticket();
		newTicket.setUser(user);
		newTicket.setEvent(event);
		newTicket.setStatus(Ticket.Status.PENDING);
		newTicket.setRating(null);

		return ticketRepository.save(newTicket);
	}

	public Optional<Ticket> findByUserAndEventAndStatus(User user, Event event, Ticket.Status status) {
		return ticketRepository.findByUserAndEventAndStatus(user, event, status);
	}

	public Optional<Ticket> findById(Long ticketId) {
		return ticketRepository.findById(ticketId);
	}

	public void updateTicketStatus(Ticket ticket, Ticket.Status status) {
		ticket.setStatus(status);
		ticketRepository.save(ticket);
	}

	public Double calculateRevenueByEvent(Long eventId) {
		return ticketRepository.calculateRevenueByEvent(eventId);
	}
}
