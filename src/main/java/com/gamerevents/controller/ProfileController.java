package com.gamerevents.controller;

import com.gamerevents.model.Profile;
import com.gamerevents.model.User;
import com.gamerevents.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = findUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("profile", user.getProfile());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String username,
                                @RequestParam(required = false, defaultValue = "") String avatarUrl,
                                @RequestParam(required = false, defaultValue = "") String favoriteGames,
                                @RequestParam(defaultValue = "false") boolean notificationsEnabled,
                                RedirectAttributes redirectAttributes) {
        User user = findUser(userDetails);
        Profile profile = user.getProfile();

        if (profile == null) {
            profile = new Profile(user);
            user.setProfile(profile);
        }

        profile.setUsername(username);
        profile.setAvatarUrl(avatarUrl);
        profile.setFavoriteGames(favoriteGames);
        profile.setNotificationsEnabled(notificationsEnabled);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("updated", "Profile updated successfully.");
        return "redirect:/profile";
    }

    private User findUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }
}
