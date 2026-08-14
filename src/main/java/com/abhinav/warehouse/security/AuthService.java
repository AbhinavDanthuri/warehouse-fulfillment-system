package com.abhinav.warehouse.security;

import com.abhinav.warehouse.entity.User;
import com.abhinav.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("email already registered");
        }

        User user = User.builder()
                .email(req.email())
                // BCrypt salts each hash internally — no salt column needed, and
                // two users with the same password get different hashes.
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(req.role())
                .build();

        userRepository.save(user);
        return new AuthResponse(
                jwtService.issue(user.getEmail(), user.getRole().name()),
                user.getEmail(),
                user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        try {
            // Delegating to the AuthenticationManager rather than comparing hashes
            // by hand means the timing-safe comparison and lockout hooks come free.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("invalid email or password");
        }

        var user = userRepository.findByEmail(req.email()).orElseThrow();
        return new AuthResponse(
                jwtService.issue(user.getEmail(), user.getRole().name()),
                user.getEmail(),
                user.getRole().name());
    }
}
