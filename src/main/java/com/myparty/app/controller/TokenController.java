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
import com.myparty.app.controller.dto.LoginRequestDto;
import com.myparty.app.controller.dto.LoginResponseDto;
import com.myparty.app.service.UserService;
import jakarta.validation.Valid;

@RestController
public class TokenController {

	private final JwtEncoder jwtEncoder;
	private final UserService userService;
	private BCryptPasswordEncoder passwordEncoder;

	public TokenController(BCryptPasswordEncoder passwordEncoder, UserService userService, JwtEncoder jwtEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.jwtEncoder = jwtEncoder;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

		var user = userService.findByUsername(loginRequestDto.username());

		if (user.isEmpty() || !user.get().isLoginCorrect(loginRequestDto, passwordEncoder)) {
			throw new BadCredentialsException("username or password is invalid!");
		}

		var now = Instant.now();
		var expiresIn = 3000L;

		var scope = user.get().getRole();

		var claims = JwtClaimsSet.builder()
				.issuer("mypartydb")
				.subject(user.get().getUserId().toString())
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expiresIn))
				.claim("scope", scope)
				.build();

		var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return ResponseEntity.ok(new LoginResponseDto(jwtValue, expiresIn));
	}

}
