package com.myparty.app.entities;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_events")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long eventId;

	@ManyToOne
	@JoinColumn(name = "organizer_id", referencedColumnName = "user_id")
	private User organizer;

	@Column(unique = true)
	private String title;

	private String description;

	private String location;

	private Instant date;

	private Double price;

	private String category;

	private Double rating;

	private Long reviews;

	public Event() {
	}

	public Event(Long eventId, User organizer, String title, String description, String location, Instant date, Double price, String category, Double rating, Long reviews) {
		this.eventId = eventId;
		this.organizer = organizer;
		this.title = title;
		this.description = description;
		this.location = location;
		this.date = date;
		this.price = price;
		this.category = category;
		this.rating = rating;
		this.reviews = reviews;
	}

	public Long getReviews() {
		return reviews;
	}

	public void setReviews(Long reviews) {
		this.reviews = reviews;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public User getOrganizer() {
		return organizer;
	}

	public void setOrganizer(User organizer) {
		this.organizer = organizer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Instant getDate() {
		return date;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Long getReviews() { return reviews; }

	public void setReviews(Long reviews) { this.reviews = reviews; }

	public List<Ticket> getTickets() { return tickets; }

	public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}
