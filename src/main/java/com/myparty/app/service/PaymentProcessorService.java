package com.myparty.app.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.myparty.app.entities.Ticket;

@Service
public class PaymentProcessorService {

	private final TicketService ticketService;
	private final Random random = new Random();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public PaymentProcessorService(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@Async
	public void processPayment(Ticket ticket) {
		scheduler.schedule(() -> {
			if (ticket.getStatus() != Ticket.Status.PENDING) {
				return;
			}
			boolean approved = random.nextBoolean();
			ticketService.updateTicketStatus(ticket, approved ? Ticket.Status.APPROVED : Ticket.Status.REJECTED);

			// TODO: send an SMS to the user with the result
		}, 10, TimeUnit.SECONDS);
	}
}
