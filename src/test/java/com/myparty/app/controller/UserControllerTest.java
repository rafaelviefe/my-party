package com.myparty.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.myparty.app.controller.dto.CreateUserDto;
import com.myparty.app.controller.dto.UpdatePasswordDto;
import com.myparty.app.controller.dto.UpdateUserDto;
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

	@Test
	void shouldFailWhenCreatingUserWithEmptyUsername() {

		var dto = new CreateUserDto(
				"",
				"test-password",
				"test-phone",
				false
		);

		webTestClient.post()
			.uri("/users")
			.bodyValue(dto)
			.exchange()
			.expectStatus().isUnauthorized();
	}

	@Sql("/insert.sql")
	@Test
	void shouldRetrieveAllUsers() {
		webTestClient.get()
			.uri("/users")
			.header("Authorization", "Bearer " + token)
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.length()").isEqualTo(4);
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
	void shouldReturnNotFoundWhenNotFindingUserById() {
		webTestClient.get()
			.uri("/users/5f74ce15-982a-4326-8365-80e800d772f5")
			.header("Authorization", "Bearer " + token)
			.exchange()
			.expectStatus().isNotFound();
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdateUserSuccessfully() {

		var dto = new UpdateUserDto(
				"adminUpdated",
				"new-phone",
				false
		);

		webTestClient.put()
				.uri("/users")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingUserWithDuplicateUsername() {

		var dto = new UpdateUserDto(
				"rafael",
				"new-phone",
				false
		);

		webTestClient.put()
				.uri("/users")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingUserWithEmptyUsername() {

		var dto = new UpdateUserDto(
				"",
				"new-phone",
				false
		);

		webTestClient.put()
				.uri("/users")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdateOtherUserSuccessfully() {

		var dto = new UpdateUserDto(
				"UserUpdated",
				"new-phone",
				true
		);

		webTestClient.put()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingOtherUserWithDuplicateUsername() {

		var dto = new UpdateUserDto(
				"admin",
				"new-phone",
				false
		);

		webTestClient.put()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingOtherUserWithEmptyUsername() {

		var dto = new UpdateUserDto(
				"",
				"new-phone",
				false
		);

		webTestClient.put()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdatePasswordSuccessfully() {

		var dto = new UpdatePasswordDto(
				"123",
				"321"
		);

		webTestClient.patch()
				.uri("/users")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isNoContent();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingPasswordWithIncorrectOldPassword() {

		var dto = new UpdatePasswordDto(
				"222",
				"321"
		);

		webTestClient.patch()
				.uri("/users")
				.header("Authorization", "Bearer " + token)
				.bodyValue(dto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldUpdateUserRoleSuccessfully() {

		webTestClient.patch()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8/ORGANIZER")
				.header("Authorization", "Bearer " + token)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingUserRoleWithInvalidRole() {

		webTestClient.patch()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8/INVALID_ROLE")
				.header("Authorization", "Bearer " + token)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenUpdatingUserRoleToTheSameRole() {

		webTestClient.patch()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f8/PARTICIPANT")
				.header("Authorization", "Bearer " + token)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Sql("/insert.sql")
	@Test
	void shouldDeleteUserSuccessfully() {

		webTestClient.delete()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f6")
				.header("Authorization", "Bearer " + token)
				.exchange()
				.expectStatus().isOk();
	}

	@Sql("/insert.sql")
	@Test
	void shouldFailWhenDeletingUserWithTickets() {

		webTestClient.delete()
				.uri("/users/5f74ce15-982a-4326-8365-80e800d772f7")
				.header("Authorization", "Bearer " + token)
				.exchange()
				.expectStatus().is4xxClientError();
	}
}
