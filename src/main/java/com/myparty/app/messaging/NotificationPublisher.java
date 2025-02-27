package com.myparty.app.messaging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.myparty.app.entities.Notification;

@Component
public class NotificationPublisher {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange}")
	private String exchange;

	@Value("${rabbitmq.routingKey}")
	private String routingKey;

	public NotificationPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishNotification(String phoneNumber, String message, Instant sendAt, Notification.NotificationType type) {
		Map<String, Object> notificationData = new HashMap<>();
		notificationData.put("phoneNumber", phoneNumber);
		notificationData.put("message", message);
		notificationData.put("sendAt", sendAt.toString());
		notificationData.put("type", type.name());

		rabbitTemplate.convertAndSend(exchange, routingKey, notificationData);
	}
}
