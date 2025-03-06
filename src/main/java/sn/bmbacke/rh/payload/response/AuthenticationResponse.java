package sn.bmbacke.rh.payload.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
    private List<Long> roles;
    private String email;
    private String fullName;
}