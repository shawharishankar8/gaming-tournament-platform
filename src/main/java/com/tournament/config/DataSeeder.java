package com.tournament.config;

import com.tournament.model.entity.User;
import com.tournament.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSeeder {

    @Bean
    @Profile("default")  // Only for H2 development
    public CommandLineRunner seedH2Data(UserRepository userRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                createAdminUser(userRepository, passwordEncoder);
                System.out.println("✅ H2: Admin user created for development");
                System.out.println("👤 Admin: username=admin, password=admin123");
            }
        };
    }

    @Bean
    @Profile("docker")  // Only for Docker - ensures admin exists
    public CommandLineRunner ensureAdminUser(UserRepository userRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                createAdminUser(userRepository, passwordEncoder);
                System.out.println("✅ Docker: Admin user created");
                System.out.println("👤 Admin: username=admin, password=admin123");
            } else {
                System.out.println("✅ Docker: Admin user already exists");
            }
        };
    }

    private void createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@tournament.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        userRepository.save(admin);
    }
}