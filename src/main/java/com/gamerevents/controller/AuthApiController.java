package com.gamerevents.controller;

import com.gamerevents.dto.AuthRequest;
import com.gamerevents.dto.AuthResponse;
import com.gamerevents.dto.RegisterRequest;
import com.gamerevents.model.Profile;
import com.gamerevents.model.User;
import com.gamerevents.repository.UserRepository;
import com.gamerevents.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthApiController(AuthenticationManager authenticationManager,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()), "USER");
        Profile profile = new Profile(user);
        profile.setUsername(request.name());
        profile.setNotificationsEnabled(true);
        user.setProfile(profile);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return new AuthResponse(jwtService.generateToken(authentication), "Bearer");
    }
}
