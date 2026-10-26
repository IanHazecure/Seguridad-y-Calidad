package com.gamerevents.controller;

import com.gamerevents.model.Profile;
import com.gamerevents.model.User;
import com.gamerevents.repository.EventRepository;
import com.gamerevents.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageController(EventRepository eventRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredEvents", eventRepository.findByFeaturedTrueOrderByStartDateAsc());
        model.addAttribute("upcomingEvents", eventRepository.findAllByOrderByStartDateAsc());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email is already registered.");
            return "redirect:/register";
        }

        User user = new User(name, email, passwordEncoder.encode(password), "USER");
        Profile profile = new Profile(user);
        profile.setUsername(name);
        profile.setNotificationsEnabled(true);
        user.setProfile(profile);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("registered", "Registration completed. You can now log in.");
        return "redirect:/login";
    }
}
