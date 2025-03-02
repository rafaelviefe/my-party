package com.myparty.app.controller.dto;

import java.time.Instant;
import com.myparty.app.entities.Notification;

public record NotificationDto(
		String phoneNumber,
		String message,
		Instant sendAt,
		Notification.NotificationType type
) {
}
