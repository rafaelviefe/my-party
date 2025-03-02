package com.myparty.app.service;

import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.myparty.app.entities.Notification;
import com.myparty.app.repository.NotificationRepository;

@Component
public class NotificationSchedulerService {

	private final NotificationRepository notificationRepository;
	private final TwilioService twilioService;

	public NotificationSchedulerService(NotificationRepository notificationRepository, TwilioService twilioService) {
		this.notificationRepository = notificationRepository;
		this.twilioService = twilioService;
	}

	@Scheduled(fixedRate = 60000)
	public void checkAndSendNotifications() {
		Instant now = Instant.now();
		List<Notification> notifications = notificationRepository.findBySendAtBeforeAndSentFalse(now);

		for (Notification notification : notifications) {
			boolean sent = twilioService.sendSMS(notification.getPhoneNumber(), notification.getMessage());
			if (sent) {
				notification.setSent(true);
				notificationRepository.save(notification);
			}
		}
	}
}
