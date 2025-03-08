package com.myparty.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.myparty.app.controller.dto.CreateUserDto;
import com.myparty.app.utils.JwtTestUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/remove.sql")
public class UserControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private JwtTestUtil jwtTestUtil;

	private String token;

	@BeforeEach
	void setUp() {
		token = jwtTestUtil.generateToken("ADMIN", "5f74ce15-982a-4326-8365-80e800d772f7");
	}

	@Test
	void shouldCreateUserSuccessfully() {

		var dto = new CreateUserDto(
			"test-user",
			"test-password",
			"test-phone",
			false
		);

		webTestClient.post()
			.uri("/users")
			.bodyValue(dto)
			.exchange()
			.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenCreatingUserWithDuplicateUsername() {

		var dto = new CreateUserDto(
				"rafael",
				"test-password",
				"test-phone",
				false
		);

		webTestClient.post()
			.uri("/users")
			.bodyValue(dto)
			.exchange()
			.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveAllUsers() {
		webTestClient.get()
			.uri("/users")
			.header("Authorization", "Bearer " + token)
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.length()").isEqualTo(3);
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveUserByIdSuccessfully() {
		webTestClient.get()
			.uri("/users/5f74ce15-982a-4326-8365-80e800d772f7")
			.header("Authorization", "Bearer " + token)
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.username").isEqualTo("admin");
	}

	@Sql("/insert.sql")
	@Test
	void shouldReturnNotFoundWhenUserDoesNotExist() {
		webTestClient.get()
			.uri("/users/5f74ce15-982a-4326-8365-80e800d772f6")
			.header("Authorization", "Bearer " + token)
			.exchange()
			.expectStatus().isNotFound();
	}


}
