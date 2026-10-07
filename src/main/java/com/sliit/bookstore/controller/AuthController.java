package com.sliit.bookstore.controller;

import com.sliit.bookstore.dto.JwtResponse;
import com.sliit.bookstore.dto.LoginRequest;
import com.sliit.bookstore.dto.RegisterRequest;
import com.sliit.bookstore.model.Role;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.model.PasswordResetToken;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.repository.PasswordResetTokenRepository;
import com.sliit.bookstore.security.JwtUtil;
import com.sliit.bookstore.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String rawRole = userDetails.getAuthorities().iterator().next().getAuthority();
        // Strip the "ROLE_" prefix that Spring Security adds internally
        String role = rawRole.startsWith("ROLE_") ? rawRole.substring(5) : rawRole;

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = java.util.UUID.randomUUID().toString();
            
            PasswordResetToken resetToken = new PasswordResetToken(user, token, java.time.LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(resetToken);
            
            // Print the URL to the console for testing purposes
            String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;
            System.out.println("==================================================");
            System.out.println("PASSWORD RESET REQUEST FOR: " + email);
            System.out.println("RESET URL: " + resetUrl);
            System.out.println("==================================================");
            
            return ResponseEntity.ok(Map.of("message", "Password reset instructions have been sent."));
        } else {
            // Standard practice is to pretend it was sent to not leak emails
            return ResponseEntity.ok(Map.of("message", "Password reset instructions have been sent."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || newPassword == null || token.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token and new password are required"));
        }
        
        var tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isUsedStatus() || resetToken.getExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is invalid or has expired"));
        }
        
        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        
        resetToken.setUsedStatus(true);
        tokenRepository.save(resetToken);
        
        return ResponseEntity.ok(Map.of("message", "Password reset successful. Please login."));
    }
}
