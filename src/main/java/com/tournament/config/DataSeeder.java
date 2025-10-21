package com.tournament.config;

import com.tournament.model.entity.User;
import com.tournament.model.entity.Player;
import com.tournament.model.entity.Tournament;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.model.enums.TournamentType;
import com.tournament.repository.UserRepository;
import com.tournament.repository.PlayerRepository;
import com.tournament.repository.TournamentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
@Profile("!test")  // Don't run in tests
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PlayerRepository playerRepository,
                                      TournamentRepository tournamentRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Only seed if no users exist
            if (userRepository.count() == 0) {
                // Create Admin User
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@tournament.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);

                // Create Sample Players
                String[] players = {
                        "john_doe:JohnTheDestroyer:1450",
                        "sarah_gamer:SarahQueen:1620",
                        "mike_pro:MikeThePro:1580",
                        "lisa_champ:LisaChampion:1750"
                };

                for (String playerData : players) {
                    String[] parts = playerData.split(":");
                    String username = parts[0];
                    String gamerTag = parts[1];
                    int elo = Integer.parseInt(parts[2]);

                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(username + "@example.com");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRole("PLAYER");
                    userRepository.save(user);

                    Player player = new Player();
                    player.setUser(user);
                    player.setGamerTag(gamerTag);
                    player.setEloRating(elo);
                    player.setWins(20 + elo / 50);
                    player.setLosses(10 + elo / 100);
                    playerRepository.save(player);
                }

                // Create Sample Tournaments
                Tournament tournament1 = new Tournament();
                tournament1.setName("Spring Championship 2024");
                tournament1.setType(TournamentType.SINGLE_ELIMINATION);
                tournament1.setMaxParticipants(16);
                tournament1.setEntryFee(new BigDecimal("25.00"));
                tournament1.setPrizePool(new BigDecimal("400.00"));
                tournament1.setStatus(TournamentStatus.UPCOMING);
                tournament1.setStartTime(OffsetDateTime.now().plusDays(7));
                tournamentRepository.save(tournament1);

                Tournament tournament2 = new Tournament();
                tournament2.setName("Weekly Quick Tournament");
                tournament2.setType(TournamentType.SINGLE_ELIMINATION);
                tournament2.setMaxParticipants(8);
                tournament2.setEntryFee(new BigDecimal("10.00"));
                tournament2.setPrizePool(new BigDecimal("80.00"));
                tournament2.setStatus(TournamentStatus.UPCOMING);
                tournament2.setStartTime(OffsetDateTime.now().plusDays(2));
                tournamentRepository.save(tournament2);

                System.out.println("✅ Sample data seeded successfully!");
                System.out.println("👤 Admin: username=admin, password=admin123");
                System.out.println("🎮 Players: username=john_doe/sarah_gamer/mike_pro/lisa_champ, password=password123");
            }
        };
    }
}
