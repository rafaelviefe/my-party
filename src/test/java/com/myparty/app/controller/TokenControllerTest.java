package com.myparty.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.myparty.app.controller.dto.LoginRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/remove.sql")
public class TokenControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Sql("/insert.sql")
	@Test
	void shouldLoginSuccessfully() {
		var dto = new LoginRequestDto("admin", "123");

		webTestClient.post()
			.uri("/login")
			.bodyValue(dto)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.accessToken").isNotEmpty()
			.jsonPath("$.expiresIn").isEqualTo(3000L);
	}
}
