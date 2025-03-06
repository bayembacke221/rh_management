package sn.bmbacke.rh.payload.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.Role;
import sn.bmbacke.rh.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private LocalDateTime lastLogin;
    private boolean accountLocked;
    private boolean enabled;

    public static UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }

        String fullName = null;
        if (user.getEmployee() != null) {
            fullName = user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName();
        }

        List<String> roleNames = null;
        if (user.getRoles() != null) {
            roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(fullName)
                .roles(roleNames)
                .lastLogin(user.getLastLogin())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .build();
    }
}