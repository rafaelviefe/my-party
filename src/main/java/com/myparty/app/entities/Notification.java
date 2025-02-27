package com.myparty.app.entities;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String phoneNumber;

	private String message;

	private Instant sendAt;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	private boolean sent = false;

	public enum NotificationType {
		EVENT_REMINDER,
		EVENT_CANCELLATION,
		PAYMENT_REJECTION,
		PAYMENT_APPROVAL
	}

	public Notification() {
	}

	public Notification(Long id, String phoneNumber, String message, Instant sendAt, NotificationType type, boolean sent) {
		this.id = id;
		this.phoneNumber = phoneNumber;
		this.message = message;
		this.sendAt = sendAt;
		this.type = type;
		this.sent = sent;
	}

	@Override public String toString() {
		return "Notification{" +
				"id=" + id +
				", phoneNumber='" + phoneNumber + '\'' +
				", message='" + message + '\'' +
				", sendAt=" + sendAt +
				", type=" + type +
				", sent=" + sent +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Instant getSendAt() {
		return sendAt;
	}

	public void setSendAt(Instant sendAt) {
		this.sendAt = sendAt;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}
}
