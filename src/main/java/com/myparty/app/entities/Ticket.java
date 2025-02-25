package com.myparty.app.entities;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

	private Double rating;

	public Ticket() {
	}

	public Ticket(Long ticketId, User user, Event event, Status status, Double rating) {
		this.ticketId = ticketId;
		this.user = user;
		this.event = event;
		this.status = status;
		this.rating = rating;
	}

	public static DoubleSummaryStatistics calculateRatingStatistics(List<Ticket> tickets) {
		return tickets.stream()
				.map(Ticket::getRating)
				.filter(Objects::nonNull)
				.collect(Collectors.summarizingDouble(Double::doubleValue));
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

	public Double getRating() { return rating; }

	public void setRating(Double rating) { this.rating = rating; }

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
