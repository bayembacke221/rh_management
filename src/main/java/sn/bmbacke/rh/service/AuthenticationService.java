package sn.bmbacke.rh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import sn.bmbacke.rh.entity.Role;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupérer l'utilisateur authentifié
        User user = (User) authentication.getPrincipal();

        // Mettre à jour la date de dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Préparer les claims pour le token
        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.getFullName());

        // Générer le token JWT
        String jwtToken = jwtUtil.generateToken(claims, user);

        // Récupérer les IDs des rôles de l'utilisateur
        List<Long> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        // Sauvegarder le token dans la base de données
        Token token = Token.builder()
                .token(jwtToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();
        tokenRepository.save(token);

        // Construire et retourner la réponse
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .roles(roleIds)
                .email(user.getEmail())
                .fullName(user.getFullName())
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

    @Transactional
    public void logout(String token) {
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        storedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(storedToken);

        log.info("Token révoqué avec succès: {}", token);
    }
}