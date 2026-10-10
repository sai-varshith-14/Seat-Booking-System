package com.platform.seatbooking.service;

import com.platform.seatbooking.dto.AuthResponse;
import com.platform.seatbooking.dto.LoginRequest;
import com.platform.seatbooking.dto.RegisterRequest;
import com.platform.seatbooking.entity.User;
import com.platform.seatbooking.enums.Role;
import com.platform.seatbooking.repository.UserRepository;
import com.platform.seatbooking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
                .role(Role.USER) // Default role
                .build();
        userRepository.save(user);

        var jwtToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(), java.util.List.of()
                )
        );
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(), java.util.List.of()
                )
        );
        return AuthResponse.builder().token(jwtToken).build();
    }
}
