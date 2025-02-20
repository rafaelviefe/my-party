package com.myparty.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private Long ticketId;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "event_id", referencedColumnName = "event_id")
	private Event event;

	@Enumerated(EnumType.STRING)
	private Status status;

	public Ticket() {
	}

	public Long getTicketId() {
		return ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public enum Status {
		PENDING("Pending"),
		APPROVED("Approved"),
		REJECTED("Rejected");

		private final String displayName;

		Status(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public static Status fromString(String status) {
			for (Status s : Status.values()) {
				if (s.name().equalsIgnoreCase(status)) {
					return s;
				}
			}
			throw new IllegalArgumentException("Invalid status: " + status);
		}
	}
}
