package com.myparty.app.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.myparty.app.controller.dto.EventResponseDto;
import com.myparty.app.entities.Event;
import com.myparty.app.entities.Notification;
import com.myparty.app.entities.Ticket;
import com.myparty.app.entities.User;
import com.myparty.app.messaging.NotificationPublisher;
import com.myparty.app.repository.EventRepository;

@Service
public class EventService {

	private final EventRepository eventRepository;
	private final TicketService ticketService;
	private final NotificationPublisher notificationPublisher;

	public EventService(EventRepository eventRepository, TicketService ticketService, NotificationPublisher notificationPublisher) {
		this.eventRepository = eventRepository;
		this.ticketService = ticketService;
		this.notificationPublisher = notificationPublisher;
	}

	public void save(Event event) {eventRepository.save(event);}

	public Optional<Event> findById(Long id) {return eventRepository.findById(id);}

	public Optional<Event> findByTitle(String title) {return eventRepository.findByTitle(title);}

	public List<Event> findAll() {return eventRepository.findAll();}

	public void deleteById(Event event) {

		String eventTitle = event.getTitle();
		List<User> users = ticketService.getTicketsByEvent(event)
				.stream().map(Ticket::getUser).toList();

		eventRepository.deleteById(event.getEventId());

		notifyEventCancellation(users, eventTitle);
	}

	private void notifyEventCancellation(List<User> users, String eventTitle) {

		for (User user : users) {
			String message = user.getUsername() + ", we regret to inform you that the event '" + eventTitle + "' has been canceled.";

			notificationPublisher.publishNotification(
					user.getPhoneNumber(),
					message,
					Instant.now(),
					Notification.NotificationType.EVENT_CANCELLATION
			);
		}
	}

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

	@Scheduled(cron = "0 0 8 * * *")
	public void checkAndSendEventNotifications() {

		LocalDate targetDate = LocalDate.now().plusDays(3);
		ZoneId zoneId = ZoneId.systemDefault();

		Instant startOfDay = targetDate.atStartOfDay(zoneId).toInstant();
		Instant endOfDay = targetDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();

		List<Event> events = eventRepository.findByDateBetween(startOfDay, endOfDay);

		for (Event event : events) {
			List<User> users = ticketService.getTicketsByEvent(event)
					.stream().map(Ticket::getUser).toList();

			for (User user : users) {
				String message = user.getUsername() + ", the event '" + event.getTitle() + "' will occur in 3 days.";

				notificationPublisher.publishNotification(
						user.getPhoneNumber(),
						message,
						Instant.now(),
						Notification.NotificationType.EVENT_REMINDER
				);
			}
		}
	}


}
