package com.cognevance.ecommerce.service;

import com.cognevance.ecommerce.dto.AuthDtos.*;
import com.cognevance.ecommerce.entity.Role;
import com.cognevance.ecommerce.entity.User;
import com.cognevance.ecommerce.repository.UserRepository;
import com.cognevance.ecommerce.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        Role role = (req.role != null) ? req.role : Role.CUSTOMER;
        User user = new User(req.name, req.email, passwordEncoder.encode(req.password), role);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email, req.password));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getUsername(), user.getRole());
    }
}
