package com.myparty.app.controller;

import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.myparty.app.controller.dto.LoginRequest;
import com.myparty.app.controller.dto.LoginResponse;
import com.myparty.app.service.TokenService;
import jakarta.validation.Valid;

@RestController("/login")
public class TokenController {

	private final JwtEncoder jwtEncoder;
	private final TokenService tokenService;
	private BCryptPasswordEncoder passwordEncoder;

	public TokenController(BCryptPasswordEncoder passwordEncoder, TokenService tokenService, JwtEncoder jwtEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.jwtEncoder = jwtEncoder;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

		var user = tokenService.findByUsername(loginRequest.username());

		if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
			throw new BadCredentialsException("username or password is invalid!");
		}

		var now = Instant.now();
		var expiresIn = 3000L;

		var scopes = user.get().getRole();

		var claims = JwtClaimsSet.builder()
				.issuer("mypartydb")
				.subject(user.get().getUserId().toString())
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expiresIn))
				.claim("scope", scopes)
				.build();

		var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn));
	}

}
