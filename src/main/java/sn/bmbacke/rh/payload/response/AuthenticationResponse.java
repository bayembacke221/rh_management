package sn.bmbacke.rh.payload.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private List<Long> roles;
}
