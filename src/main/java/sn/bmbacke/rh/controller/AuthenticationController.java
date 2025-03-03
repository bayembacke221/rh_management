package sn.bmbacke.rh.controller;

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
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserDTO currentUser = service.getCurrentUser(authentication);
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String token) {
        String jwtToken = token.substring(7);
        service.logout(jwtToken);
        return ResponseEntity.ok().build();
    }
}
