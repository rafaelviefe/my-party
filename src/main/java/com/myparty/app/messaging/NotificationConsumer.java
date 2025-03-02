package com.myparty.app.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.myparty.app.controller.dto.NotificationDto;
import com.myparty.app.entities.Notification;
import com.myparty.app.repository.NotificationRepository;

@Component
@RabbitListener(queues = "${rabbitmq.queue}")
public class NotificationConsumer {

	private final NotificationRepository notificationRepository;

	public NotificationConsumer(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@RabbitListener(queues = "${rabbitmq.queue}")
	public void receiveNotification(NotificationDto notificationDTO) {
		Notification notification = new Notification();
		notification.setPhoneNumber(notificationDTO.phoneNumber());
		notification.setMessage(notificationDTO.message());
		notification.setSendAt(notificationDTO.sendAt());
		notification.setType(notificationDTO.type());

		notificationRepository.save(notification);
	}
}
