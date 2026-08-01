package com.cristopher.reservas_api.service;

import com.cristopher.reservas_api.dto.request.LoginRequest;
import com.cristopher.reservas_api.dto.request.RegisterRequest;
import com.cristopher.reservas_api.dto.response.AuthResponse;
import com.cristopher.reservas_api.entity.Role;
import com.cristopher.reservas_api.entity.User;
import com.cristopher.reservas_api.exception.EmailAlreadyExistsException;
import com.cristopher.reservas_api.repository.UserRepository;
import com.cristopher.reservas_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}