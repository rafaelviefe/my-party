package com.myparty.app.utils;

import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtTestUtil {

	private final JwtEncoder jwtEncoder;

	public JwtTestUtil(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String generateToken(String role, String subject) {
		var now = Instant.now();
		var expiresIn = 3000L;

		var claims = JwtClaimsSet.builder()
				.issuer("mypartydb")
				.subject(subject)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expiresIn))
				.claim("scope", role)
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
