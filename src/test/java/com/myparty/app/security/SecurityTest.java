package com.myparty.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.myparty.app.utils.JwtTestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/remove.sql")
public class SecurityTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private JwtTestUtil jwtTestUtil;

	private String adminToken;
	private String participantToken;
	private String organizerToken;

	@BeforeEach
	void setUp() {
		adminToken = jwtTestUtil.generateToken("ADMIN", "5f74ce15-982a-4326-8365-80e800d772f7");
		participantToken = jwtTestUtil.generateToken("PARTICIPANT", "5f74ce15-982a-4326-8365-80e800d772f8");
		organizerToken = jwtTestUtil.generateToken("ORGANIZER", "5f74ce15-982a-4326-8365-80e800d772f9");
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenParticipantTriesTOGetUsers() {
		webTestClient.get()
			.uri("/users")
			.header("Authorization", "Bearer " + participantToken)
			.exchange()
			.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenOrganizerTriesToChangeRoleOfAdmin() {
		webTestClient.patch()
			.uri("/users/5f74ce15-982a-4326-8365-80e800d772f7/ORGANIZER")
			.header("Authorization", "Bearer " + organizerToken)
			.exchange()
			.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenOrganizerTriesToDeleteUser() {
		webTestClient.delete()
			.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8")
			.header("Authorization", "Bearer " + organizerToken)
			.exchange()
			.expectStatus().isForbidden();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenTokenIsInvalid() {
		webTestClient.get()
				.uri("/users")
				.header("Authorization", "Bearer " + "invalid-token")
				.exchange()
				.expectStatus().isUnauthorized();
	}

}
