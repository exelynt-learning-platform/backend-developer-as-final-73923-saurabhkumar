package com.booking.config;

import com.booking.model.*;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.user-password:user123}")
    private String userPassword;

    public DataSeeder(UserRepository userRepository,
                      ResourceRepository resourceRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedResources();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@booking.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            logger.info("Seeded ADMIN user: admin");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@booking.com")
                    .password(passwordEncoder.encode(userPassword))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            logger.info("Seeded USER user: user");
        }
    }

    private void seedResources() {
        if (resourceRepository.count() == 0) {
            resourceRepository.save(Resource.builder()
                    .name("Conference Room A")
                    .description("Large conference room with projector and whiteboard, seats 20")
                    .type("ROOM")
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Company Van")
                    .description("8-seater passenger van for team transport")
                    .type("VEHICLE")
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Projector HD-500")
                    .description("Portable HD projector with HDMI and USB-C connectivity")
                    .type("EQUIPMENT")
                    .available(true)
                    .build());

            logger.info("Seeded 3 sample resources");
        }
    }
}
