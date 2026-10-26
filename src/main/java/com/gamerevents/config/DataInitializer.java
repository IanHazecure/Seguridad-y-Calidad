package com.gamerevents.config;

import com.gamerevents.model.Event;
import com.gamerevents.model.Game;
import com.gamerevents.model.Profile;
import com.gamerevents.model.User;
import com.gamerevents.repository.EventRepository;
import com.gamerevents.repository.GameRepository;
import com.gamerevents.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(UserRepository userRepository,
                                     GameRepository gameRepository,
                                     EventRepository eventRepository) {
        return args -> {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            User firstUser = createUser(userRepository, passwordEncoder,
                    "Chape", "test1@game.com", "gamer123", "USER");
            User secondUser = createUser(userRepository, passwordEncoder,
                    "fakeperson", "test2@game.com", "gamer123", "USER");
            User thirdUser = createUser(userRepository, passwordEncoder,
                    "fuckleffen", "test3@game.com", "gamer123", "ADMIN");

            createProfile(userRepository, firstUser, "Chape", "Super Smash Bros. Melee", true);
            createProfile(userRepository, secondUser, "fakeperson", "Street Fighter 6", true);
            createProfile(userRepository, thirdUser, "fuckleffen", "Mario Kart 8 Deluxe", false);

            Game melee = gameRepository.findByName("Super Smash Bros. Melee")
                    .orElseGet(() -> gameRepository.save(
                            new Game("Super Smash Bros. Melee", "Nintendo GameCube")));

            if (eventRepository.count() == 0) {
                Event event = new Event(
                        "Zenkoku Taikai 5: Ultimate Chapter",
                        "Super Smash Bros. Melee tournament.",
                        LocalDateTime.of(2027, 1, 30, 9, 0),
                        LocalDateTime.of(2027, 1, 31, 18, 0),
                        "Chile",
                        "Av. Pedro de Valdivia 164, Providencia",
                        "Tournament"
                );
                event.setFeatured(true);
                event.setParkingAvailable(true);
                event.setPublicTransportAvailable(true);
                event.setOrganizer("Zenkoku Taikai Team");
                event.setCreatedBy(thirdUser);
                event.getGames().add(melee);
                eventRepository.save(event);
            }
        };
    }

    private User createUser(UserRepository userRepository,
                            BCryptPasswordEncoder passwordEncoder,
                            String name,
                            String email,
                            String password,
                            String role) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
                new User(name, email, passwordEncoder.encode(password), role)));
    }

    private void createProfile(UserRepository userRepository,
                               User user,
                               String username,
                               String favoriteGames,
                               boolean notificationsEnabled) {
        if (user.getProfile() == null) {
            Profile profile = new Profile(user);
            profile.setUsername(username);
            profile.setFavoriteGames(favoriteGames);
            profile.setNotificationsEnabled(notificationsEnabled);
            user.setProfile(profile);
            userRepository.save(user);
        }
    }
}
