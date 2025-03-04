package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.mapper.UserDTO;
import sn.bmbacke.rh.payload.request.AuthenticationRequest;
import sn.bmbacke.rh.payload.response.AuthenticationResponse;
import sn.bmbacke.rh.service.AuthenticationService;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API de gestion de l'authentification")
public class AuthenticationController {

    private final AuthenticationService service;


    @Operation(
            summary = "Authentification",
            description = "Authentification d'un utilisateur"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @ApiResponse(responseCode = "401", description = "Authentification échouée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Parameter(description = "Requête d'authentification")
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Operation(
            summary = "Obtenir l'utilisateur courant",
            description = "Obtenir les informations de l'utilisateur courant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur courant obtenu avec succès"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(
            @Parameter(description = "Authentification de l'utilisateur")
            Authentication authentication) {
        UserDTO currentUser = service.getCurrentUser(authentication);
        return ResponseEntity.ok(currentUser);
    }

    @Operation(
            summary = "Déconnexion",
            description = "Déconnexion de l'utilisateur courant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Token d'authentification")
            @RequestHeader(name = "Authorization") String token) {
        String jwtToken = token.substring(7);
        service.logout(jwtToken);
        return ResponseEntity.ok().build();
    }
}
