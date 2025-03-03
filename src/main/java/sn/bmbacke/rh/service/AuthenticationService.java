package sn.bmbacke.rh.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.bmbacke.rh.entity.Token;
import sn.bmbacke.rh.entity.User;
import sn.bmbacke.rh.exception.TokenNotFoundException;
import sn.bmbacke.rh.exception.UnauthorizedException;
import sn.bmbacke.rh.payload.mapper.UserDTO;
import sn.bmbacke.rh.payload.request.AuthenticationRequest;
import sn.bmbacke.rh.payload.response.AuthenticationResponse;
import sn.bmbacke.rh.repository.TokenRepository;
import sn.bmbacke.rh.repository.UserRepository;
import sn.bmbacke.rh.security.JwtUtil;
import sn.bmbacke.rh.validation.ValidationService;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    private final ValidationService validationService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var claims = new HashMap<String, Object>();
        var user = ((User)authentication.getPrincipal());
        claims.put("fullName", user.getFullName());

        var jwtToken = jwtUtil.generateToken(claims,user);
        var roleUser = user.getRoles().stream().map(role -> (long) role.getId()).toList();

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .roles(roleUser)
                .build();
    }




    public UserDTO getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return UserDTO.convertToDTO(user);
    }

    public void logout(String token) {
        // Invalider le token
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        storedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(storedToken);
    }

}
