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
import com.myparty.app.controller.dto.CreateTicketDto;
import com.myparty.app.entities.Notification;
import com.myparty.app.messaging.NotificationPublisher;
import com.myparty.app.utils.JwtTestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/remove.sql")
public class TicketControllerTest {

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
		participantToken = jwtTestUtil.generateToken("PARTICIPANT", "5f74ce15-982a-4326-8365-80e800d772f6");
	}

	@Sql("/insert.sql")
	@Test
	void shouldCreateNewTicketSuccessfully() {
		doNothing().when(notificationPublisher).publishNotification(any(String.class), any(String.class), any(Instant.class), any(Notification.NotificationType.class));

		var dto = new CreateTicketDto(
				"Tech Stars 2025"
		);

		webTestClient.post()
			.uri("/tickets")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldNotCreateNewTicketWhenEventHasAlreadyHappened() {
		var dto = new CreateTicketDto(
				"Soccer Cup 2025"
		);

		webTestClient.post()
			.uri("/tickets")
			.header("Authorization", "Bearer " + participantToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldNotCreateNewTicketWhenUserAlreadyHasTicketForEvent() {
		var dto = new CreateTicketDto(
				"Music Fest 2025"
		);

		webTestClient.post()
			.uri("/tickets")
			.header("Authorization", "Bearer " + adminToken)
			.bodyValue(dto)
			.exchange()
			.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveAllTicketsSuccessfully() {
		webTestClient.get()
			.uri("/tickets")
			.header("Authorization", "Bearer " + adminToken)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(8);
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveTicketsByUserSuccessfully() {
		webTestClient.get()
			.uri("/tickets/me")
			.header("Authorization", "Bearer " + adminToken)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(2);
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveRatingStatisticsSuccessfully() {
		webTestClient.get()
			.uri("/tickets/rating-statistics")
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
	void shouldRetryRejectedTicketSuccessfully() {
		doNothing().when(notificationPublisher).publishNotification(any(String.class), any(String.class), any(Instant.class), any(Notification.NotificationType.class));

		webTestClient.patch()
			.uri("/tickets/3/retry")
			.header("Authorization", "Bearer " + participantToken)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnForbiddenWhenRetryingTicketWithDifferentUser() {
		webTestClient.patch()
				.uri("/tickets/3/retry")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnBadRequestWhenRetryingTicketWithDifferentStatus() {
		webTestClient.patch()
				.uri("/tickets/6/retry")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldCancelTicketSuccessfully() {
		webTestClient.patch()
			.uri("/tickets/2")
			.header("Authorization", "Bearer " + adminToken)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnForbiddenWhenCancelingTicketWithUninvolvedUser() {
		webTestClient.patch()
				.uri("/tickets/5")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnBadRequestWhenCancelingTicketWithRejectedStatus() {
		webTestClient.patch()
				.uri("/tickets/8")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnBadRequestWhenCancelingTicketWithPastEvent() {
		webTestClient.patch()
				.uri("/tickets/7")
				.header("Authorization", "Bearer " + adminToken)
				.exchange()
				.expectStatus().isBadRequest();
	}
}
