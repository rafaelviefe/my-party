package com.myparty.app.service;

import org.springframework.stereotype.Service;
import com.myparty.app.entities.Ticket;
import com.myparty.app.repository.TicketRepository;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;

	public TicketService(TicketRepository ticketRepository) {this.ticketRepository = ticketRepository;}

	public void save(Ticket ticket) {ticketRepository.save(ticket);}

}
