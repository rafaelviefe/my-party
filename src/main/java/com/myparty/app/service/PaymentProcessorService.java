package com.myparty.app.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.myparty.app.entities.Notification;
import com.myparty.app.entities.Ticket;
import com.myparty.app.messaging.NotificationPublisher;

@Service
public class PaymentProcessorService {

	private final TicketService ticketService;
	private final Random random = new Random();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public final NotificationPublisher notificationPublisher;

	public PaymentProcessorService(TicketService ticketService, NotificationPublisher notificationPublisher) {
		this.ticketService = ticketService;
		this.notificationPublisher = notificationPublisher;
	}

	@Async
	public void processPayment(Ticket ticket, Ticket.Status status) {
		scheduler.schedule(() -> {
			if (ticket.getStatus() != status) {
				return;
			}

			boolean approved = random.nextBoolean();
			Ticket.Status newStatus = approved ? Ticket.Status.APPROVED : Ticket.Status.REJECTED;
			ticketService.updateTicketStatus(ticket, newStatus);

			sendNotification(ticket, approved);

		}, 10, TimeUnit.SECONDS);
	}

	private void sendNotification(Ticket ticket, boolean approved) {
		var message = approved
				? "Your payment for the event '" + ticket.getEvent().getTitle() + "' has been APPROVED! Your ticket is guaranteed."
				: "Unfortunately, your payment for the event '" + ticket.getEvent().getTitle() + "' has been REJECTED. Please try again.";

		var type = approved ? Notification.NotificationType.PAYMENT_APPROVAL : Notification.NotificationType.PAYMENT_REJECTION;

		notificationPublisher.publishNotification(
				ticket.getUser().getPhoneNumber(),
				message,
				Instant.now(),
				type
		);
	}
}
