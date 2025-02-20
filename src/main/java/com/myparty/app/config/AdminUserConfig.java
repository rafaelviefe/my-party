package com.myparty.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.myparty.app.entities.User;
import com.myparty.app.repository.UserRepository;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	public AdminUserConfig(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		var userAdmin = userRepository.findByUsername("admin");

		userAdmin.ifPresentOrElse(
			(user) ->	System.out.println("admin user already exists"),
			() -> {
				var admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("123"));
				admin.setRole(User.Role.ADMIN);
				admin.setStudent(false);
				admin.setPhoneNumber("+1234567890");
				userRepository.save(admin);;
				System.out.println("admin user created: " + admin);
			}
		);

	}
}
