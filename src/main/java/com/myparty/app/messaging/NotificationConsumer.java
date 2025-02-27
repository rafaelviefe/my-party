package com.myparty.app.messaging;

import java.time.Instant;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.myparty.app.entities.Notification;
import com.myparty.app.repository.NotificationRepository;

@Component
@RabbitListener(queues = "${rabbitmq.queue}")
public class NotificationConsumer {

	private final NotificationRepository notificationRepository;

	public NotificationConsumer(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@RabbitHandler
	public void receiveMessage(Map<String, Object> message) {
		Notification notification = new Notification();
		notification.setPhoneNumber(message.get("phoneNumber").toString());
		notification.setMessage(message.get("message").toString());
		notification.setSendAt(Instant.parse(message.get("sendAt").toString()));
		notification.setType(Notification.NotificationType.valueOf(message.get("type").toString()));

		notificationRepository.save(notification);
	}
}
