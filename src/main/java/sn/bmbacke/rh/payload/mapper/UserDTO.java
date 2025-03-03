package sn.bmbacke.rh.payload.mapper;


import lombok.*;
import sn.bmbacke.rh.entity.Role;
import sn.bmbacke.rh.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private List<String> roles;
    private boolean enabled;

    public static UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getEmployee().getFirstName())
                .lastname(user.getEmployee().getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .enabled(user.isEnabled())
                .build();
    }
}
