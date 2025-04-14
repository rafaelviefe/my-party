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
import com.myparty.app.config.SecurityConfig;
import com.myparty.app.controller.dto.LoginRequestDto;
import com.myparty.app.controller.dto.LoginResponseDto;
import com.myparty.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Token", description = "Token management")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class TokenController {

	private final JwtEncoder jwtEncoder;
	private final UserService userService;
	private final BCryptPasswordEncoder passwordEncoder;

	public TokenController(BCryptPasswordEncoder passwordEncoder, UserService userService, JwtEncoder jwtEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
		this.jwtEncoder = jwtEncoder;
	}

	@Operation(summary = "Login", description = "Method to login a user")
	@ApiResponse(responseCode = "200", description = "User logged in successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input - validation error")
	@ApiResponse(responseCode = "401", description = "Unauthorized - invalid username or password")
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

		var user = userService.findByUsername(loginRequestDto.username());

		if (user.isEmpty() || user.get().passwordMatches(loginRequestDto.password(), passwordEncoder)) {
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
