package com.myparty.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.myparty.app.controller.dto.RatingEventDto;
import com.myparty.app.controller.dto.RequestEventDto;
import com.myparty.app.entities.Notification;
import com.myparty.app.messaging.NotificationPublisher;
import com.myparty.app.utils.JwtTestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/remove.sql")
public class EventControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private JwtTestUtil jwtTestUtil;

	@MockitoBean
	private NotificationPublisher notificationPublisher;

	private String adminToken;
	private String participantToken;

	@BeforeEach
	void setUp() {
		adminToken = jwtTestUtil.generateToken("ADMIN", "5f74ce15-982a-4326-8365-80e800d772f7");
		participantToken = jwtTestUtil.generateToken("PARTICIPANT", "5f74ce15-982a-4326-8365-80e800d772f8");
	}

	@Sql("/insert.sql")
	@Test
	void shouldCreateNewEventSuccessfully() {
		var dto = new RequestEventDto(
			"test-event",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.post()
			.uri("/events")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenCreatingEventWithDuplicateTitle() {
		var dto = new RequestEventDto(
			"Soccer Cup 2025",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.post()
			.uri("/events")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenCreatingEventWithEmptyTitle() {
		var dto = new RequestEventDto(
			"",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.post()
			.uri("/events")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveAllEventsSuccessfully() {
		webTestClient.get()
				.uri("/events")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(3);
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveEventByIdSuccessfully() {
		webTestClient.get()
				.uri("/events/2")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.title").isEqualTo("Tech Stars 2025");
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnNotFoundWhenNotFindingEventById() {
		webTestClient.get()
				.uri("/events/10")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveEventRevenueSuccessfully() {
		webTestClient.get()
				.uri("/events/4/revenue")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.eventId").isEqualTo(4)
				.jsonPath("$.revenue").isEqualTo(79.99);
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveEventRatingStatisticsSuccessfully() {
		webTestClient.get()
				.uri("/events/rating-statistics")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.count").isEqualTo(1)
				.jsonPath("$.sum").isEqualTo(4)
				.jsonPath("$.min").isEqualTo(4)
				.jsonPath("$.max").isEqualTo(4)
				.jsonPath("$.average").isEqualTo(4);
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdateEventSuccessfully() {
		var dto = new RequestEventDto(
			"Tech Stars 2025",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.put()
			.uri("/events/2")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingEventWithDuplicateTitle() {
		var dto = new RequestEventDto(
			"Soccer Cup 2025",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.put()
			.uri("/events/2")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingEventWithEmptyTitle() {
		var dto = new RequestEventDto(
			"",
			"test-description",
			"test-location",
			Instant.now(),
			10.0,
			"test-category"
		);

		webTestClient.put()
			.uri("/events/2")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdateEventOrganizerSuccessfully() {
		webTestClient.patch()
			.uri("/events/2/5f74ce15-982a-4326-8365-80e800d772f9")
			.header("Authorization", "Bearer " + adminToken)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingEventOrganizerWithDifferentOrganizer() {
		webTestClient.patch()
			.uri("/events/3/5f74ce15-982a-4326-8365-80e800d772f7")
			.header("Authorization", "Bearer " + adminToken)
			.exchange()
			.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRateEventSuccessfully() {

		var dto = new RatingEventDto(4.5);

		webTestClient.patch()
				.uri("/events/4/rating")
				.header("Authorization", "Bearer " + adminToken)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenRatingEventWithInvalidRating() {

		var dto = new RatingEventDto(4.7);

		webTestClient.patch()
				.uri("/events/4/rating")
				.header("Authorization", "Bearer " + adminToken)
				.bodyValue(dto)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenRatingEventWithFutureEvent() {

		var dto = new RatingEventDto(4.5);

		webTestClient.patch()
				.uri("/events/2/rating")
				.header("Authorization", "Bearer " + adminToken)
				.bodyValue(dto)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenRatingEventWithInvalidTicket() {

		var dto = new RatingEventDto(4.5);

		webTestClient.patch()
				.uri("/events/4/rating")
				.header("Authorization", "Bearer " + participantToken)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Sql("/insert.sql")
	@Test
	void shouldDeleteEventSuccessfully() {
		doNothing().when(notificationPublisher).publishNotification(any(String.class), any(String.class), any(Instant.class), any(Notification.NotificationType.class));

		webTestClient.delete()
				.uri("/events/2")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenDeletingEventWithDifferentOrganizer() {
		doNothing().when(notificationPublisher).publishNotification(any(String.class), any(String.class), any(Instant.class), any(Notification.NotificationType.class));

		webTestClient.delete()
				.uri("/events/3")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isForbidden();
	}

}
